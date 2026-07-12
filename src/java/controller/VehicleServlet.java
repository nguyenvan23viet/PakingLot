package controller;

import dao.UserDAO;
import dao.VehicleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.User;
import model.Vehicle;
import utils.PaginationUtil;
import utils.Validator;

/**
 * Handles Vehicles:
 *  - ADMIN: view all, add, update, delete, search
 *  - STAFF: view all, add, update, search (no delete)
 *  - CUSTOMER: view/add/update only their OWN vehicles, search restricted to own plates
 */
@WebServlet(name = "VehicleServlet", urlPatterns = {"/admin/VehicleServlet", "/staff/VehicleServlet", "/customer/VehicleServlet"})
public class VehicleServlet extends HttpServlet {

    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final UserDAO userDAO = new UserDAO();

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
                request.setAttribute("keyword", keyword);
                List<Vehicle> found = "CUSTOMER".equals(role)
                        ? vehicleDAO.searchVehiclesByOwner(currentUser.getUserID(), keyword)
                        : vehicleDAO.searchVehicles(keyword);
                setPaginatedVehicles(request, found);
                break;
            }
            case "showEdit": {
                int id = Integer.parseInt(request.getParameter("id"));
                Vehicle v = vehicleDAO.getVehicleByID(id);
                if ("CUSTOMER".equals(role) && !isOwnedBy(v, currentUser)) {
                    // Blocks a customer from editing someone else's vehicle by guessing the id in the URL.
                    response.sendRedirect(buildRoleUrl(request, role));
                    return;
                }
                request.setAttribute("editVehicle", v);
                loadListForRole(request, currentUser, role);
                break;
            }
            default:
                loadListForRole(request, currentUser, role);
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

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "add":
                handleAddOrUpdate(request, response, currentUser, role, false);
                return;
            case "update":
                handleAddOrUpdate(request, response, currentUser, role, true);
                return;
            case "delete": {
                if (!"ADMIN".equals(role)) {
                    // Only ADMIN may delete. Staff/Customer never see this button, but
                    // guard here too in case someone posts the URL directly.
                    response.sendRedirect(buildRoleUrl(request, role));
                    return;
                }
                int id = Integer.parseInt(request.getParameter("id"));
                boolean ok = vehicleDAO.deleteVehicle(id);
                if (!ok) {
                    request.setAttribute("error",
                            "Khong the xoa xe nay vi da co lich su gui xe (ParkingTickets) lien quan.");
                    loadListForRole(request, currentUser, role);
                    forwardByRole(request, response, role);
                    return;
                }
                response.sendRedirect(buildRoleUrl(request, role));
                return;
            }
            default:
                response.sendRedirect(buildRoleUrl(request, role));
        }
    }

    private void handleAddOrUpdate(HttpServletRequest request, HttpServletResponse response,
            User currentUser, String role, boolean isUpdate) throws ServletException, IOException {

        String licensePlate = request.getParameter("licensePlate");
        String vehicleType = request.getParameter("vehicleType");
        String brand = request.getParameter("brand");
        String color = request.getParameter("color");
        String ownerName = request.getParameter("ownerName");
        String ownerPhone = request.getParameter("ownerPhone");
        String vehicleIdStr = request.getParameter("vehicleID");

        String ownerIdStr = null;
        Integer ownerID;
        String ownerIdError = null;
        if ("CUSTOMER".equals(role)) {
            // A customer always registers/edits a vehicle under their own account.
            ownerID = currentUser.getUserID();
        } else {
            ownerIdStr = request.getParameter("ownerID");
            if (ownerIdStr == null || ownerIdStr.trim().isEmpty()) {
                ownerID = null;
            } else {
                ownerIdError = Validator.validatePositiveInt(ownerIdStr.trim(), "OwnerID");
                ownerID = (ownerIdError == null) ? Integer.parseInt(ownerIdStr.trim()) : null;
            }
        }

        // Ownership check BEFORE anything else, when a customer edits an existing vehicle.
        if (isUpdate && "CUSTOMER".equals(role)) {
            Vehicle current = vehicleDAO.getVehicleByID(Integer.parseInt(vehicleIdStr));
            if (!isOwnedBy(current, currentUser)) {
                response.sendRedirect(buildRoleUrl(request, role));
                return;
            }
        }

        String error = (ownerIdError != null) ? ownerIdError
                : validateVehicleInput(licensePlate, vehicleType, ownerID, ownerName, ownerPhone);

        if (error == null) {
            Vehicle existing = vehicleDAO.getVehicleByPlate(licensePlate.trim());
            if (existing != null) {
                boolean sameRecord = isUpdate && vehicleIdStr != null
                        && existing.getVehicleID() == Integer.parseInt(vehicleIdStr);
                if (!sameRecord) {
                    error = "Bien so xe nay da ton tai trong he thong.";
                }
            }
        }

        if (error != null) {
            request.setAttribute("error", error);
            loadListForRole(request, currentUser, role);
            request.setAttribute("formLicensePlate", licensePlate);
            request.setAttribute("formVehicleType", vehicleType);
            request.setAttribute("formBrand", brand);
            request.setAttribute("formColor", color);
            request.setAttribute("formOwnerName", ownerName);
            request.setAttribute("formOwnerPhone", ownerPhone);
            if (isUpdate) {
                request.setAttribute("formVehicleID", vehicleIdStr);
            }
            forwardByRole(request, response, role);
            return;
        }

        Vehicle v = new Vehicle();
        v.setLicensePlate(licensePlate.trim());
        v.setVehicleType(vehicleType);
        v.setBrand(brand);
        v.setColor(color);
        v.setOwnerID(ownerID);
        v.setOwnerName(ownerName);
        v.setOwnerPhone(ownerPhone);

        if (isUpdate) {
            v.setVehicleID(Integer.parseInt(vehicleIdStr));
            vehicleDAO.updateVehicle(v);
        } else {
            vehicleDAO.insertVehicle(v);
        }

        response.sendRedirect(buildRoleUrl(request, role));
    }

    private String validateVehicleInput(String licensePlate, String vehicleType,
            Integer ownerID, String ownerName, String ownerPhone) {
        String error = Validator.validateLicensePlate(licensePlate);
        if (error == null) {
            error = Validator.validateRequired(vehicleType, "Loai xe");
        }
        // A walk-in vehicle (no linked account) must at least have an owner name.
        if (error == null && ownerID == null) {
            error = Validator.validateRequired(ownerName, "Ten chu xe (xe khong gan tai khoan)");
        }
        // OwnerPhone is optional, but if provided it must be a valid phone format.
        if (error == null && ownerPhone != null && !ownerPhone.trim().isEmpty()) {
            error = Validator.validatePhone(ownerPhone);
        }
        return error;
    }

    private boolean isOwnedBy(Vehicle v, User user) {
        return v != null && v.getOwnerID() != null && v.getOwnerID() == user.getUserID();
    }

    private void loadListForRole(HttpServletRequest request, User currentUser, String role) {
        List<Vehicle> fullList = "CUSTOMER".equals(role)
                ? vehicleDAO.getVehiclesByOwner(currentUser.getUserID())
                : vehicleDAO.getAllVehicles();
        setPaginatedVehicles(request, fullList);
    }

    /** Slices the full list down to the requested page and exposes paging info to the JSP. */
    private void setPaginatedVehicles(HttpServletRequest request, List<Vehicle> fullList) {
        enrichOwnerContactInfo(fullList);
        int page = parsePage(request.getParameter("page"));
        PaginationUtil.PageResult<Vehicle> result
                = PaginationUtil.paginate(fullList, page, PaginationUtil.DEFAULT_PAGE_SIZE);
        request.setAttribute("vehicles", result.getItems());
        request.setAttribute("currentPage", result.getCurrentPage());
        request.setAttribute("totalPages", result.getTotalPages());
    }

    /**
     * For vehicles linked to a registered account (OwnerID != null), always display the
     * OWNER'S CURRENT Name/Phone from the Users table - not the Vehicles.OwnerName/OwnerPhone
     * columns, which are meant only for walk-in vehicles with no account. This also fixes the
     * case where a customer registered their own vehicle (those 2 columns were left blank).
     * This only overwrites the in-memory objects for display - nothing is written back to DB.
     */
    private void enrichOwnerContactInfo(List<Vehicle> vehicles) {
        for (Vehicle v : vehicles) {
            if (v.getOwnerID() != null) {
                User owner = userDAO.getUserByID(v.getOwnerID());
                if (owner != null) {
                    v.setOwnerName(owner.getFullName());
                    v.setOwnerPhone(owner.getPhone());
                }
            }
        }
    }

    private int parsePage(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 1;
        }
    }

    private void forwardByRole(HttpServletRequest request, HttpServletResponse response, String role)
            throws ServletException, IOException {
        String view;
        switch (role) {
            case "ADMIN":
                view = "/admin/vehicles.jsp";
                break;
            case "STAFF":
                view = "/staff/vehicles.jsp";
                break;
            default:
                view = "/customer/vehicles.jsp";
        }
        request.getRequestDispatcher(view).forward(request, response);
    }

    private String buildRoleUrl(HttpServletRequest request, String role) {
        String contextPath = request.getContextPath();
        switch (role) {
            case "ADMIN":
                return contextPath + "/admin/VehicleServlet";
            case "STAFF":
                return contextPath + "/staff/VehicleServlet";
            default:
                return contextPath + "/customer/VehicleServlet";
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session == null) ? null : (User) session.getAttribute("user");
    }
}