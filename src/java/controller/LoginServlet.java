package controller;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;

/**
 * Handles the login form submitted from index.jsp.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // If someone GETs this URL directly, just show the login page.
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Vui long nhap day du Ten dang nhap va Mat khau.");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        UserDAO dao = new UserDAO();
        User user = dao.login(username.trim(), password);

        if (user == null) {
            request.setAttribute("error", "Sai ten dang nhap hoac mat khau, hoac tai khoan da bi khoa.");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        // Login OK: store the whole User object in session so JSPs/Servlets can read it.
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("role", user.getRole());

        String contextPath = request.getContextPath();
        switch (user.getRole()) {
            case "ADMIN":
                response.sendRedirect(contextPath + "/admin/home.jsp");
                break;
            case "STAFF":
                response.sendRedirect(contextPath + "/staff/home.jsp");
                break;
            case "CUSTOMER":
                response.sendRedirect(contextPath + "/customer/home.jsp");
                break;
            default:
                response.sendRedirect(contextPath + "/index.jsp");
        }
    }
}