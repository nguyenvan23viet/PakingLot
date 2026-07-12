package controller;

import dao.ParkingRateDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.ParkingRate;
import model.User;
import utils.PaginationUtil;
import utils.Validator;

/**
 * Handles ParkingRates: full CRUD for ADMIN, read-only list/search for STAFF and CUSTOMER.
 * Mapped under all three role folders so AuthorizationFilter enforces access per role,
 * while all business logic stays here (JSP only renders).
 */
@WebServlet(name = "RateServlet", urlPatterns = {"/admin/RateServlet", "/staff/RateServlet", "/customer/RateServlet"})
public class RateServlet extends HttpServlet {

    private final ParkingRateDAO rateDAO = new ParkingRateDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (session == null) ? null : (User) session.getAttribute("user");
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
                request.setAttribute("keyword", keyword);
                setPaginatedRates(request, rateDAO.searchRates(keyword));
                break;
            }
            case "showEdit": {
                if (!"ADMIN".equals(role)) {
                    response.sendRedirect(buildRoleUrl(request, role));
                    return;
                }
                int id = Integer.parseInt(request.getParameter("id"));
                request.setAttribute("editRate", rateDAO.getRateByID(id));
                setPaginatedRates(request, rateDAO.getAllRates());
                break;
            }
            default:
                setPaginatedRates(request, rateDAO.getAllRates());
        }

        forwardByRole(request, response, role);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (session == null) ? null : (User) session.getAttribute("user");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        String role = currentUser.getRole();

        // Only ADMIN may add/edit/delete. Staff/Customer never see this form, but
        // we still guard here in case someone posts the URL directly.
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
                rateDAO.deleteRate(id);
                response.sendRedirect(request.getContextPath() + "/admin/RateServlet");
                return;
            }
            default:
                response.sendRedirect(request.getContextPath() + "/admin/RateServlet");
        }
    }

    private void handleAddOrUpdate(HttpServletRequest request, HttpServletResponse response, boolean isUpdate)
            throws ServletException, IOException {
        String vehicleType = request.getParameter("vehicleType");
        String rateType = request.getParameter("rateType");
        String priceStr = request.getParameter("price");
        String description = request.getParameter("description");
        String rateIdStr = request.getParameter("rateID");

        String error = validateRateInput(vehicleType, rateType, priceStr);

        double price = 0;
        if (error == null) {
            price = Double.parseDouble(priceStr.trim());

            // Duplicate rule: a VehicleType + RateType combination must be unique.
            ParkingRate existing = rateDAO.getRateByType(vehicleType, rateType);
            if (existing != null) {
                boolean sameRecord = isUpdate && rateIdStr != null
                        && existing.getRateID() == Integer.parseInt(rateIdStr);
                if (!sameRecord) {
                    error = "Da ton tai muc gia cho loai xe va hinh thuc tinh phi nay.";
                }
            }
        }

        if (error != null) {
            request.setAttribute("error", error);
            setPaginatedRates(request, rateDAO.getAllRates());
            // Re-populate the form with what the user typed, so nothing is lost.
            request.setAttribute("formVehicleType", vehicleType);
            request.setAttribute("formRateType", rateType);
            request.setAttribute("formPrice", priceStr);
            request.setAttribute("formDescription", description);
            if (isUpdate) {
                request.setAttribute("formRateID", rateIdStr);
            }
            request.getRequestDispatcher("/admin/rates.jsp").forward(request, response);
            return;
        }

        ParkingRate r = new ParkingRate();
        r.setVehicleType(vehicleType);
        r.setRateType(rateType);
        r.setPrice(price);
        r.setDescription(description);

        if (isUpdate) {
            r.setRateID(Integer.parseInt(rateIdStr));
            rateDAO.updateRate(r);
        } else {
            rateDAO.insertRate(r);
        }

        // Redirect (not forward) after a successful write, to avoid duplicate
        // submission if the user refreshes the page.
        response.sendRedirect(request.getContextPath() + "/admin/RateServlet");
    }

    private String validateRateInput(String vehicleType, String rateType, String priceStr) {
        String error = Validator.validateRequired(vehicleType, "Loai xe");
        if (error == null) {
            error = Validator.validateRequired(rateType, "Hinh thuc tinh phi");
        }
        if (error == null) {
            error = Validator.validatePositiveDouble(priceStr, "Don gia");
        }
        return error;
    }

    private void forwardByRole(HttpServletRequest request, HttpServletResponse response, String role)
            throws ServletException, IOException {
        String view;
        switch (role) {
            case "ADMIN":
                view = "/admin/rates.jsp";
                break;
            case "STAFF":
                view = "/staff/rates.jsp";
                break;
            default:
                view = "/customer/rates.jsp";
        }
        request.getRequestDispatcher(view).forward(request, response);
    }

    private String buildRoleUrl(HttpServletRequest request, String role) {
        String contextPath = request.getContextPath();
        switch (role) {
            case "ADMIN":
                return contextPath + "/admin/RateServlet";
            case "STAFF":
                return contextPath + "/staff/RateServlet";
            default:
                return contextPath + "/customer/RateServlet";
        }
    }

    /** Slices the full list down to the requested page and exposes paging info to the JSP. */
    private void setPaginatedRates(HttpServletRequest request, List<ParkingRate> fullList) {
        int page = parsePage(request.getParameter("page"));
        PaginationUtil.PageResult<ParkingRate> result
                = PaginationUtil.paginate(fullList, page, PaginationUtil.DEFAULT_PAGE_SIZE);
        request.setAttribute("rates", result.getItems());
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