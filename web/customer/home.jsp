<%-- 
    Document   : home
    Created on : Jul 4, 2026, 9:45:32 PM
    Author     : nguye
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Trang chu Khach Hang</title>
    </head>
    <body>
        <%
            User user = (User) session.getAttribute("user");
        %>
        <h2>Xin chao, <%= user.getFullName()%> (Customer)</h2>
        <p><a href="<%= request.getContextPath()%>/LogoutServlet">Dang xuat</a></p>
    </body>
</html>
