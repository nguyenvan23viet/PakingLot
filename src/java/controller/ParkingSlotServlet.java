package controller;

import dao.ParkingSlotDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.ParkingSlot;
import model.User;
import utils.PaginationUtil;
import utils.Validator;

/**
 * Handles ParkingSlots: full CRUD for ADMIN, read-only list/search for STAFF.
 * CUSTOMER has no access at all (per the permission matrix), so this servlet
 * is only mapped under /admin/ and /staff/ — there is no /customer/ variant.
 */
@WebServlet(name = "ParkingSlotServlet", urlPatterns = {"/admin/ParkingSlotServlet", "/staff/ParkingSlotServlet"})
public class ParkingSlotServlet extends HttpServlet {

    private final ParkingSlotDAO slotDAO = new ParkingSlotDAO();

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
            case "search": {
                String keyword = request.getParameter("keyword");
                String status = request.getParameter("status");
                request.setAttribute("keyword", keyword);
                request.setAttribute("status", status);
                setPaginatedSlots(request, slotDAO.searchSlots(keyword, status));
                break;
            }
            case "showEdit": {
                if (!"ADMIN".equals(role)) {
                    response.sendRedirect(buildRoleUrl(request, role));
                    return;
                }
                int id = Integer.parseInt(request.getParameter("id"));
                request.setAttribute("editSlot", slotDAO.getSlotByID(id));
                setPaginatedSlots(request, slotDAO.getAllSlots());
                break;
            }
            default:
                setPaginatedSlots(request, slotDAO.getAllSlots());
        }

        forwardByRole(request, response, role);
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

        // Only ADMIN may add/edit/delete parking slots.
        if (!"ADMIN".equals(role)) {
            response.sendRedirect(buildRoleUrl(request, role));
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "add":
                handleAddOrUpdate(request, response, false);
                return;
            case "update":
                handleAddOrUpdate(request, response, true);
                return;
            case "delete": {
                int id = Integer.parseInt(request.getParameter("id"));
                boolean ok = slotDAO.deleteSlot(id);
                if (!ok) {
                    request.setAttribute("error",
                            "Khong the xoa vi tri nay vi da co lich su gui xe (ParkingTickets) lien quan.");
                    setPaginatedSlots(request, slotDAO.getAllSlots());
                    request.getRequestDispatcher("/admin/slots.jsp").forward(request, response);
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/admin/ParkingSlotServlet");
                return;
            }
            default:
                response.sendRedirect(request.getContextPath() + "/admin/ParkingSlotServlet");
        }
    }

    private void handleAddOrUpdate(HttpServletRequest request, HttpServletResponse response, boolean isUpdate)
            throws ServletException, IOException {
        String slotCode = request.getParameter("slotCode");
        String slotType = request.getParameter("slotType");
        String status = request.getParameter("status");
        String slotIdStr = request.getParameter("slotID");

        String error = validateSlotInput(slotCode, slotType, status);

        if (error == null) {
            // Duplicate check: SlotCode must be unique.
            for (ParkingSlot existing : slotDAO.getAllSlots()) {
                boolean codeMatches = existing.getSlotCode().equalsIgnoreCase(slotCode.trim());
                boolean sameRecord = isUpdate && slotIdStr != null
                        && existing.getSlotID() == Integer.parseInt(slotIdStr);
                if (codeMatches && !sameRecord) {
                    error = "Ma vi tri nay da ton tai.";
                    break;
                }
            }
        }

        if (error != null) {
            request.setAttribute("error", error);
            setPaginatedSlots(request, slotDAO.getAllSlots());
            request.setAttribute("formSlotCode", slotCode);
            request.setAttribute("formSlotType", slotType);
            request.setAttribute("formStatus", status);
            if (isUpdate) {
                request.setAttribute("formSlotID", slotIdStr);
            }
            request.getRequestDispatcher("/admin/slots.jsp").forward(request, response);
            return;
        }

        ParkingSlot s = new ParkingSlot();
        s.setSlotCode(slotCode.trim());
        s.setSlotType(slotType);
        s.setStatus(status);

        if (isUpdate) {
            s.setSlotID(Integer.parseInt(slotIdStr));
            slotDAO.updateSlot(s);
        } else {
            slotDAO.insertSlot(s);
        }

        response.sendRedirect(request.getContextPath() + "/admin/ParkingSlotServlet");
    }

    private String validateSlotInput(String slotCode, String slotType, String status) {
        String error = Validator.validateRequired(slotCode, "Ma vi tri");
        if (error == null) {
            error = Validator.validateRequired(slotType, "Loai xe");
        }
        if (error == null) {
            error = Validator.validateRequired(status, "Trang thai");
        }
        return error;
    }

    private void forwardByRole(HttpServletRequest request, HttpServletResponse response, String role)
            throws ServletException, IOException {
        String view = "ADMIN".equals(role) ? "/admin/slots.jsp" : "/staff/slots.jsp";
        request.getRequestDispatcher(view).forward(request, response);
    }

    private String buildRoleUrl(HttpServletRequest request, String role) {
        String contextPath = request.getContextPath();
        return "ADMIN".equals(role)
                ? contextPath + "/admin/ParkingSlotServlet"
                : contextPath + "/staff/ParkingSlotServlet";
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session == null) ? null : (User) session.getAttribute("user");
    }

    /** Slices the full list down to the requested page and exposes paging info to the JSP. */
    private void setPaginatedSlots(HttpServletRequest request, List<ParkingSlot> fullList) {
        int page = parsePage(request.getParameter("page"));
        PaginationUtil.PageResult<ParkingSlot> result
                = PaginationUtil.paginate(fullList, page, PaginationUtil.DEFAULT_PAGE_SIZE);
        request.setAttribute("slots", result.getItems());
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
}