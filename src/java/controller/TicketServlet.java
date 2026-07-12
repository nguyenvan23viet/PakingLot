package controller;

import dao.ParkingRateDAO;
import dao.ParkingSlotDAO;
import dao.ParkingTicketDAO;
import dao.VehicleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.ParkingRate;
import model.ParkingSlot;
import model.ParkingTicket;
import model.User;
import model.Vehicle;
import utils.PaginationUtil;

/**
 * Core business servlet: Check-in / Check-out, plus view/search/delete of ParkingTickets.
 *
 * ADMIN    : view all, search, delete
 * STAFF    : view all, search, CHECK-IN, CHECK-OUT
 * CUSTOMER : view/search only their own parking history
 *
 * All joins (Vehicle/Slot lookups) happen here, never in JSP, to keep business
 * logic out of the View layer per the MVC requirement.
 */
@WebServlet(name = "TicketServlet", urlPatterns = {"/admin/TicketServlet", "/staff/TicketServlet", "/customer/TicketServlet"})
public class TicketServlet extends HttpServlet {

    private final ParkingTicketDAO ticketDAO = new ParkingTicketDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final ParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private final ParkingRateDAO rateDAO = new ParkingRateDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        String role = currentUser.getRole();

        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "search":
                handleSearch(request, currentUser, role);
                forwardByRole(request, response, role);
                return;
            case "showCheckIn":
                if (!"STAFF".equals(role)) {
                    response.sendRedirect(buildRoleUrl(request, role));
                    return;
                }
                handleShowCheckIn(request);
                request.getRequestDispatcher("/staff/checkin.jsp").forward(request, response);
                return;
            case "showCheckOut":
                if (!"STAFF".equals(role)) {
                    response.sendRedirect(buildRoleUrl(request, role));
                    return;
                }
                handleShowCheckOut(request);
                request.getRequestDispatcher("/staff/checkout.jsp").forward(request, response);
                return;
            default:
                loadListForRole(request, currentUser, role);
                forwardByRole(request, response, role);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        String role = currentUser.getRole();
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "checkin":
                if (!"STAFF".equals(role)) {
                    response.sendRedirect(buildRoleUrl(request, role));
                    return;
                }
                handleCheckIn(request, response, currentUser);
                return;
            case "checkout":
                if (!"STAFF".equals(role)) {
                    response.sendRedirect(buildRoleUrl(request, role));
                    return;
                }
                handleCheckOut(request, response);
                return;
            case "delete": {
                if (!"ADMIN".equals(role)) {
                    response.sendRedirect(buildRoleUrl(request, role));
                    return;
                }
                int id = Integer.parseInt(request.getParameter("id"));
                ParkingTicket t = ticketDAO.getTicketByID(id);
                if (t != null && "PARKING".equals(t.getStatus())) {
                    // Free the slot too, otherwise it stays stuck as OCCUPIED forever.
                    slotDAO.updateSlotStatus(t.getSlotID(), "EMPTY");
                }
                ticketDAO.deleteTicket(id);
                response.sendRedirect(request.getContextPath() + "/admin/TicketServlet");
                return;
            }
            default:
                response.sendRedirect(buildRoleUrl(request, role));
        }
    }

    // ---------- CHECK-IN ----------

    /** GET preview: looks up the vehicle by plate and lists available slots. No DB write. */
    private void handleShowCheckIn(HttpServletRequest request) {
        String plate = request.getParameter("plate");
        request.setAttribute("searchedPlate", plate);
        if (plate == null || plate.trim().isEmpty()) {
            return;
        }
        Vehicle v = vehicleDAO.getVehicleByPlate(plate.trim());
        if (v == null) {
            request.setAttribute("error", "Khong tim thay xe voi bien so nay. Vui long dang ky xe truoc (menu Quan ly Xe).");
            return;
        }
        if (ticketDAO.getParkingTicketByVehicle(v.getVehicleID()) != null) {
            request.setAttribute("error", "Xe nay dang trong bai, chua check-out.");
            return;
        }
        request.setAttribute("foundVehicle", v);
        request.setAttribute("availableSlots", slotDAO.getEmptySlotsByType(v.getVehicleType()));
    }

    /** POST: actually creates the ParkingTicket and flips the slot to OCCUPIED. */
    private void handleCheckIn(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String plate = request.getParameter("plate");
        String slotIdStr = request.getParameter("slotID");
        String rateType = request.getParameter("rateType");

        Vehicle vehicle = vehicleDAO.getVehicleByPlate(plate == null ? "" : plate.trim());
        String error = null;

        if (vehicle == null) {
            error = "Khong tim thay xe voi bien so nay.";
        } else if (ticketDAO.getParkingTicketByVehicle(vehicle.getVehicleID()) != null) {
            error = "Xe nay dang trong bai, chua check-out.";
        }

        ParkingSlot slot = null;
        if (error == null) {
            slot = slotDAO.getSlotByID(Integer.parseInt(slotIdStr));
            if (slot == null || !"EMPTY".equals(slot.getStatus())) {
                error = "Vi tri da chon khong con trong, vui long chon lai.";
            } else if (!slot.getSlotType().equals(vehicle.getVehicleType())) {
                error = "Loai vi tri khong khop voi loai xe.";
            }
        }

        ParkingRate rate = null;
        if (error == null) {
            rate = rateDAO.getRateByType(vehicle.getVehicleType(), rateType);
            if (rate == null) {
                error = "Chua co bang gia cho loai xe va hinh thuc nay.";
            }
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("searchedPlate", plate);
            request.getRequestDispatcher("/staff/checkin.jsp").forward(request, response);
            return;
        }

        ParkingTicket t = new ParkingTicket();
        t.setVehicleID(vehicle.getVehicleID());
        t.setSlotID(slot.getSlotID());
        t.setStaffID(currentUser.getUserID());
        t.setRateID(rate.getRateID());
        t.setCheckInTime(new Date());

        int newTicketId = ticketDAO.checkIn(t);
        if (newTicketId > 0) {
            slotDAO.updateSlotStatus(slot.getSlotID(), "OCCUPIED");
            request.setAttribute("message", "Check-in thanh cong! Ma ve: #" + newTicketId
                    + " - Vi tri: " + slot.getSlotCode());
        } else {
            request.setAttribute("error", "Co loi xay ra luc check-in, vui long thu lai.");
        }
        request.getRequestDispatcher("/staff/checkin.jsp").forward(request, response);
    }

    // ---------- CHECK-OUT ----------

    /** GET preview: looks up the open ticket by plate and computes the fee so far. No DB write. */
    private void handleShowCheckOut(HttpServletRequest request) {
        String plate = request.getParameter("plate");
        request.setAttribute("searchedPlate", plate);
        if (plate == null || plate.trim().isEmpty()) {
            return;
        }
        Vehicle v = vehicleDAO.getVehicleByPlate(plate.trim());
        if (v == null) {
            request.setAttribute("error", "Khong tim thay xe voi bien so nay.");
            return;
        }
        ParkingTicket ticket = ticketDAO.getParkingTicketByVehicle(v.getVehicleID());
        if (ticket == null) {
            request.setAttribute("error", "Xe nay khong co phien gui dang mo (co the da check-out roi).");
            return;
        }
        ParkingRate rate = rateDAO.getRateByID(ticket.getRateID());
        double fee = calculateFee(rate, ticket.getCheckInTime(), new Date());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        request.setAttribute("previewVehicle", v);
        request.setAttribute("previewTicketID", ticket.getTicketID());
        request.setAttribute("previewCheckIn", sdf.format(ticket.getCheckInTime()));
        request.setAttribute("previewRateDesc", rate.getDescription());
        request.setAttribute("previewFee", fee);
    }

    /** POST: performs the actual check-out using the ticketID hidden field from the preview form. */
    private void handleCheckOut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String ticketIdStr = request.getParameter("ticketID");
        int ticketId = Integer.parseInt(ticketIdStr);

        ParkingTicket ticket = ticketDAO.getTicketByID(ticketId);
        if (ticket == null || !"PARKING".equals(ticket.getStatus())) {
            request.setAttribute("error", "Ve nay khong hop le hoac da check-out roi.");
            request.getRequestDispatcher("/staff/checkout.jsp").forward(request, response);
            return;
        }

        ParkingRate rate = rateDAO.getRateByID(ticket.getRateID());
        Date now = new Date();
        double fee = calculateFee(rate, ticket.getCheckInTime(), now);

        boolean ok = ticketDAO.checkOut(ticketId, now, fee);
        if (ok) {
            slotDAO.updateSlotStatus(ticket.getSlotID(), "EMPTY");
            request.setAttribute("message", "Check-out thanh cong! Tong phi: " + fee + " VND");
        } else {
            request.setAttribute("error", "Co loi xay ra luc check-out, vui long thu lai.");
        }
        request.getRequestDispatcher("/staff/checkout.jsp").forward(request, response);
    }

    /**
     * HOURLY: rounds UP to the next full hour (minimum 1 hour billed).
     * MONTHLY: flat price regardless of duration (simplification for this project).
     */
    private double calculateFee(ParkingRate rate, Date checkIn, Date checkOut) {
        if ("MONTHLY".equals(rate.getRateType())) {
            return rate.getPrice();
        }
        long diffMillis = checkOut.getTime() - checkIn.getTime();
        long hours = (long) Math.ceil(diffMillis / (1000.0 * 60 * 60));
        if (hours < 1) {
            hours = 1;
        }
        return hours * rate.getPrice();
    }

    // ---------- LIST / SEARCH ----------

    private void loadListForRole(HttpServletRequest request, User currentUser, String role) {
        List<ParkingTicket> tickets = "CUSTOMER".equals(role)
                ? ticketDAO.getTicketsByOwner(currentUser.getUserID())
                : ticketDAO.getAllTickets();
        setPaginatedRows(request, buildTicketRows(tickets));
    }

    private void handleSearch(HttpServletRequest request, User currentUser, String role) {
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String fromStr = request.getParameter("fromDate");
        String toStr = request.getParameter("toDate");

        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("fromDate", fromStr);
        request.setAttribute("toDate", toStr);

        Date from = parseDate(fromStr);
        Date to = parseDate(toStr);
        Date toInclusive = (to != null) ? new Date(to.getTime() + 24L * 60 * 60 * 1000 - 1) : null;

        List<ParkingTicket> tickets;
        if ("CUSTOMER".equals(role)) {
            tickets = filterTickets(ticketDAO.getTicketsByOwner(currentUser.getUserID()), status, from, toInclusive);
        } else {
            tickets = ticketDAO.searchTickets(keyword, status, from, toInclusive);
        }
        setPaginatedRows(request, buildTicketRows(tickets));
    }

    /** Slices the full list of pre-built rows down to the requested page. */
    private void setPaginatedRows(HttpServletRequest request, List<String[]> fullRows) {
        int page = parsePage(request.getParameter("page"));
        PaginationUtil.PageResult<String[]> result
                = PaginationUtil.paginate(fullRows, page, PaginationUtil.DEFAULT_PAGE_SIZE);
        request.setAttribute("rows", result.getItems());
        request.setAttribute("currentPage", result.getCurrentPage());
        request.setAttribute("totalPages", result.getTotalPages());
    }

    private int parsePage(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 1;
        }
    }

    /** In-memory filter used for the CUSTOMER search (their own list is always small). */
    private List<ParkingTicket> filterTickets(List<ParkingTicket> list, String status, Date from, Date to) {
        List<ParkingTicket> result = new ArrayList<>();
        String st = (status == null) ? "" : status.trim();
        for (ParkingTicket t : list) {
            if (!st.isEmpty() && !st.equals(t.getStatus())) {
                continue;
            }
            if (from != null && t.getCheckInTime().before(from)) {
                continue;
            }
            if (to != null && t.getCheckInTime().after(to)) {
                continue;
            }
            result.add(t);
        }
        return result;
    }

    private Date parseDate(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(s.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Resolves Vehicle/Slot joins here (in the Servlet) and returns plain String[] rows,
     * so the JSP only needs to print columns — no DAO calls or business logic in the View.
     * Row layout: {TicketID, LicensePlate, VehicleType, SlotCode, CheckIn, CheckOut, Fee, Status}
     */
    private List<String[]> buildTicketRows(List<ParkingTicket> tickets) {
        List<String[]> rows = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (ParkingTicket t : tickets) {
            Vehicle v = vehicleDAO.getVehicleByID(t.getVehicleID());
            ParkingSlot s = slotDAO.getSlotByID(t.getSlotID());
            String plate = (v != null) ? v.getLicensePlate() : "?";
            String vType = (v != null) ? v.getVehicleType() : "?";
            String slotCode = (s != null) ? s.getSlotCode() : "?";
            String checkIn = sdf.format(t.getCheckInTime());
            String checkOut = (t.getCheckOutTime() != null) ? sdf.format(t.getCheckOutTime()) : "";
            String fee = (t.getTotalFee() != null) ? String.valueOf(t.getTotalFee()) : "";
            rows.add(new String[]{
                String.valueOf(t.getTicketID()), plate, vType, slotCode, checkIn, checkOut, fee, t.getStatus()
            });
        }
        return rows;
    }

    private void forwardByRole(HttpServletRequest request, HttpServletResponse response, String role)
            throws ServletException, IOException {
        String view;
        switch (role) {
            case "ADMIN":
                view = "/admin/tickets.jsp";
                break;
            case "STAFF":
                view = "/staff/tickets.jsp";
                break;
            default:
                view = "/customer/tickets.jsp";
        }
        request.getRequestDispatcher(view).forward(request, response);
    }

    private String buildRoleUrl(HttpServletRequest request, String role) {
        String contextPath = request.getContextPath();
        switch (role) {
            case "ADMIN":
                return contextPath + "/admin/TicketServlet";
            case "STAFF":
                return contextPath + "/staff/TicketServlet";
            default:
                return contextPath + "/customer/TicketServlet";
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session == null) ? null : (User) session.getAttribute("user");
    }
}