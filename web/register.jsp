<%@page contentType="text/html;charset=UTF-8" language="java"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Dang Ky Tai Khoan</title>
        <style>
            body { font-family: Arial, sans-serif; }
            .register-box { width: 350px; margin: 60px auto; padding: 20px; border: 1px solid #ccc; }
            .register-box input[type="text"], .register-box input[type="password"] {
                width: 100%; padding: 6px; margin: 6px 0; box-sizing: border-box;
            }
            .error { color: red; }
        </style>
    </head>
    <body>
        <div class="register-box">
            <h2>Dang Ky Tai Khoan Khach Hang</h2>
            <%
                String error = (String) request.getAttribute("error");
                if (error != null) {
            %>
            <p class="error"><%= error%></p>
            <%
                }
                String fUsername = (String) request.getAttribute("formUsername");
                if (fUsername == null) {
                    fUsername = "";
                }
                String fFullName = (String) request.getAttribute("formFullName");
                if (fFullName == null) {
                    fFullName = "";
                }
                String fEmail = (String) request.getAttribute("formEmail");
                if (fEmail == null) {
                    fEmail = "";
                }
                String fPhone = (String) request.getAttribute("formPhone");
                if (fPhone == null) {
                    fPhone = "";
                }
            %>
            <form action="RegisterServlet" method="post">
                <label>Ten dang nhap:</label><br/>
                <input type="text" name="username" value="<%= fUsername%>" required/><br/>

                <label>Mat khau:</label><br/>
                <input type="password" name="password" required/><br/>

                <label>Nhap lai mat khau:</label><br/>
                <input type="password" name="confirmPassword" required/><br/>

                <label>Ho ten:</label><br/>
                <input type="text" name="fullName" value="<%= fFullName%>" required/><br/>

                <label>Email (khong bat buoc):</label><br/>
                <input type="text" name="email" value="<%= fEmail%>"/><br/>

                <label>So dien thoai (khong bat buoc):</label><br/>
                <input type="text" name="phone" value="<%= fPhone%>"/><br/><br/>

                <button type="submit">Dang Ky</button>
            </form>
            <p><a href="index.jsp">Da co tai khoan? Dang nhap</a></p>
        </div>
    </body>
</html>