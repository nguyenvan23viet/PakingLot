package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;

/**
 * Central authorization gate: blocks unauthenticated access, and blocks
 * cross-role access (e.g. a CUSTOMER hitting an /admin/* page directly by URL).
 *
 * Folder convention this filter relies on:
 *   /admin/*    -> ADMIN only
 *   /staff/*    -> STAFF only
 *   /customer/* -> CUSTOMER only
 * Anything else (index.jsp, LoginServlet, css/js/images) is public.
 */
@WebFilter("/*")
public class AuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // CRITICAL for Vietnamese text: must set this BEFORE any request.getParameter(...)
        // is called anywhere downstream, otherwise POST form data with dau (Vietnamese
        // diacritics) gets decoded with the wrong charset and turns into garbled characters.
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(contextPath.length()); // e.g. "/admin/home.jsp"

        if (isPublicResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User currentUser = (session == null) ? null : (User) session.getAttribute("user");

        if (currentUser == null) {
            resp.sendRedirect(contextPath + "/index.jsp");
            return;
        }

        String role = currentUser.getRole();

        if (path.startsWith("/admin/") && !"ADMIN".equals(role)) {
            resp.sendRedirect(contextPath + "/index.jsp");
            return;
        }
        if (path.startsWith("/staff/") && !"STAFF".equals(role)) {
            resp.sendRedirect(contextPath + "/index.jsp");
            return;
        }
        if (path.startsWith("/customer/") && !"CUSTOMER".equals(role)) {
            resp.sendRedirect(contextPath + "/index.jsp");
            return;
        }

        // Logged in and role matches the folder -> continue.
        chain.doFilter(request, response);
    }

    private boolean isPublicResource(String path) {
        return path.equals("/")
                || path.equals("/index.jsp")
                || path.startsWith("/LoginServlet")
                || path.startsWith("/RegisterServlet")
                || path.equals("/register.jsp")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/");
    }

    @Override
    public void destroy() {
    }
}