package controller;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.User;
import utils.Validator;

/**
 * Public self-registration. Anyone can reach this WITHOUT logging in first
 * (see AuthorizationFilter.isPublicResource). Always creates a CUSTOMER account -
 * ADMIN/STAFF accounts can only be created by an Admin via UserServlet.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/RegisterServlet"})
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");

        String error = Validator.validateUsername(username);
        if (error == null) {
            error = Validator.validatePassword(password);
        }
        if (error == null && (confirmPassword == null || !confirmPassword.equals(password))) {
            error = "Mat khau nhap lai khong khop.";
        }
        if (error == null) {
            error = Validator.validateFullName(fullName);
        }
        // Email/Phone are optional, but must be valid format if provided.
        if (error == null && email != null && !email.trim().isEmpty()) {
            error = Validator.validateEmail(email);
        }
        if (error == null && phone != null && !phone.trim().isEmpty()) {
            error = Validator.validatePhone(phone);
        }
        if (error == null && userDAO.isUsernameExists(username.trim())) {
            error = "Ten dang nhap nay da ton tai, vui long chon ten khac.";
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("formUsername", username);
            request.setAttribute("formFullName", fullName);
            request.setAttribute("formEmail", email);
            request.setAttribute("formPhone", phone);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        User u = new User();
        u.setUsername(username.trim());
        u.setPassword(password);
        u.setFullName(fullName.trim());
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole("CUSTOMER");
        u.setStatus(true);

        userDAO.insertUser(u);

        // Redirect back to the login page with a flag so it can show a success message.
        response.sendRedirect(request.getContextPath() + "/index.jsp?registered=1");
    }
}