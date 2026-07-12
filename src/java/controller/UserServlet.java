package controller;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.User;
import utils.PaginationUtil;
import utils.Validator;

/**
 * Manages Users (accounts). ADMIN only.
 * The AuthorizationFilter already blocks non-admins from /admin/*, this
 * servlet double-checks the role anyway as defense in depth.
 */
@WebServlet(name = "UserServlet", urlPatterns = {"/admin/UserServlet"})
public class UserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "search": {
                String keyword = request.getParameter("keyword");
                String role = request.getParameter("role");
                request.setAttribute("keyword", keyword);
                request.setAttribute("roleFilter", role);
                setPaginatedUsers(request, userDAO.searchUsers(keyword, role));
                break;
            }
            case "showEdit": {
                int id = Integer.parseInt(request.getParameter("id"));
                request.setAttribute("editUser", userDAO.getUserByID(id));
                setPaginatedUsers(request, userDAO.getAllUsers());
                break;
            }
            default:
                setPaginatedUsers(request, userDAO.getAllUsers());
        }

        request.getRequestDispatcher("/admin/users.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "add":
                handleAdd(request, response);
                return;
            case "update":
                handleUpdate(request, response);
                return;
            case "resetPassword":
                handleResetPassword(request, response);
                return;
            case "delete":
                handleDelete(request, response);
                return;
            default:
                response.sendRedirect(request.getContextPath() + "/admin/UserServlet");
        }
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String role = request.getParameter("role");
        boolean status = "on".equals(request.getParameter("status"));

        String error = validateAdd(username, password, fullName, email, phone, role);
        if (error == null && userDAO.isUsernameExists(username.trim())) {
            error = "Ten dang nhap nay da ton tai.";
        }

        if (error != null) {
            request.setAttribute("error", error);
            setPaginatedUsers(request, userDAO.getAllUsers());
            setFormAttrs(request, username, fullName, email, phone, role, status, null);
            request.getRequestDispatcher("/admin/users.jsp").forward(request, response);
            return;
        }

        User u = new User();
        u.setUsername(username.trim());
        u.setPassword(password);
        u.setFullName(fullName.trim());
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(role);
        u.setStatus(status);

        userDAO.insertUser(u);
        response.sendRedirect(request.getContextPath() + "/admin/UserServlet");
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userIdStr = request.getParameter("userID");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String role = request.getParameter("role");
        boolean status = "on".equals(request.getParameter("status"));

        String error = validateUpdate(fullName, email, phone, role);

        if (error != null) {
            request.setAttribute("error", error);
            setPaginatedUsers(request, userDAO.getAllUsers());
            setFormAttrs(request, null, fullName, email, phone, role, status, userIdStr);
            request.getRequestDispatcher("/admin/users.jsp").forward(request, response);
            return;
        }

        User u = new User();
        u.setUserID(Integer.parseInt(userIdStr));
        u.setFullName(fullName.trim());
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(role);
        u.setStatus(status);

        userDAO.updateUser(u);
        response.sendRedirect(request.getContextPath() + "/admin/UserServlet");
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int userId = Integer.parseInt(request.getParameter("userID"));
        String newPassword = request.getParameter("newPassword");

        String pwError = Validator.validatePassword(newPassword);
        if (pwError != null) {
            request.setAttribute("error", pwError);
            setPaginatedUsers(request, userDAO.getAllUsers());
            request.setAttribute("editUser", userDAO.getUserByID(userId));
            request.getRequestDispatcher("/admin/users.jsp").forward(request, response);
            return;
        }

        userDAO.updatePassword(userId, newPassword);
        response.sendRedirect(request.getContextPath() + "/admin/UserServlet");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));

        User currentUser = getCurrentUser(request);
        if (currentUser != null && currentUser.getUserID() == id) {
            request.setAttribute("error", "Ban khong the tu xoa tai khoan cua chinh minh.");
            setPaginatedUsers(request, userDAO.getAllUsers());
            request.getRequestDispatcher("/admin/users.jsp").forward(request, response);
            return;
        }

        boolean ok = userDAO.deleteUser(id);
        if (!ok) {
            request.setAttribute("error",
                    "Khong the xoa tai khoan nay (co the do da co Xe hoac Ve gui xe lien quan). "
                    + "Hay thu bo chon 'Dang hoat dong' de khoa tai khoan thay vi xoa.");
            setPaginatedUsers(request, userDAO.getAllUsers());
            request.getRequestDispatcher("/admin/users.jsp").forward(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin/UserServlet");
    }

    private String validateAdd(String username, String password, String fullName,
            String email, String phone, String role) {
        String error = Validator.validateUsername(username);
        if (error == null) {
            error = Validator.validatePassword(password);
        }
        if (error == null) {
            error = Validator.validateFullName(fullName);
        }
        // Email/Phone la optional trong DB - chi kiem tra dinh dang khi nguoi dung co nhap.
        if (error == null && email != null && !email.trim().isEmpty()) {
            error = Validator.validateEmail(email);
        }
        if (error == null && phone != null && !phone.trim().isEmpty()) {
            error = Validator.validatePhone(phone);
        }
        if (error == null) {
            error = Validator.validateRequired(role, "Vai tro");
        }
        return error;
    }

    private String validateUpdate(String fullName, String email, String phone, String role) {
        String error = Validator.validateFullName(fullName);
        if (error == null && email != null && !email.trim().isEmpty()) {
            error = Validator.validateEmail(email);
        }
        if (error == null && phone != null && !phone.trim().isEmpty()) {
            error = Validator.validatePhone(phone);
        }
        if (error == null) {
            error = Validator.validateRequired(role, "Vai tro");
        }
        return error;
    }

    private void setFormAttrs(HttpServletRequest request, String username, String fullName,
            String email, String phone, String role, boolean status, String userID) {
        request.setAttribute("formUsername", username);
        request.setAttribute("formFullName", fullName);
        request.setAttribute("formEmail", email);
        request.setAttribute("formPhone", phone);
        request.setAttribute("formRole", role);
        request.setAttribute("formStatus", status);
        request.setAttribute("formUserID", userID);
    }

    private boolean isAdmin(HttpServletRequest request) {
        User u = getCurrentUser(request);
        return u != null && "ADMIN".equals(u.getRole());
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session == null) ? null : (User) session.getAttribute("user");
    }

    /** Slices the full list down to the requested page and exposes paging info to the JSP. */
    private void setPaginatedUsers(HttpServletRequest request, List<User> fullList) {
        int page = parsePage(request.getParameter("page"));
        PaginationUtil.PageResult<User> result
                = PaginationUtil.paginate(fullList, page, PaginationUtil.DEFAULT_PAGE_SIZE);
        request.setAttribute("users", result.getItems());
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