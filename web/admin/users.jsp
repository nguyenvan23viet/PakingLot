<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="java.util.List"%>
<%@page import="model.User"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Quan ly Tai Khoan</title>
        <style>
            * {
                box-sizing: border-box;
            }
            body {
                margin: 0;
                font-family: Arial, sans-serif;
                background-image:
                    linear-gradient(rgba(255,255,255,0.85), rgba(255,255,255,0.85)),
                    url('https://commons.wikimedia.org/wiki/Special:FilePath/Multi-Level-Stack-Parking-in-NYC.jpg');
                background-size: cover;
                background-position: center;
                background-attachment: fixed;
                min-height: 100vh;
            }
            .content {
                max-width: 1100px;
                margin: 20px auto;
                padding: 20px;
                background: #fff;
                border-radius: 10px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.15);
            }
            .top-bar {
                display: flex;
                justify-content: space-between;
                align-items: center;
                flex-wrap: wrap;
                gap: 8px;
            }
            .table-responsive {
                width: 100%;
                overflow-x: auto;
                -webkit-overflow-scrolling: touch;
            }
            table {
                border-collapse: collapse;
                width: 100%;
                margin-top: 10px;
                min-width: 600px;
            }
            table, th, td {
                border: 1px solid #999;
                padding: 6px;
            }
            th {
                background: #f1f5f9;
            }
            .error {
                color: red;
            }

            @media (max-width: 900px) {
                .content {
                    margin: 0;
                    border-radius: 0;
                    box-shadow: none;
                    padding: 14px;
                }
                .top-bar {
                    flex-direction: column;
                    align-items: flex-start;
                }
                form input[type="text"],
                form select,
                form button {
                    width: 100%;
                    display: block;
                    margin-bottom: 8px;
                }
            }
        </style>
    </head>
    <body>
        <div class="content">
            <h2>Quan ly Tai Khoan</h2>
            <p>
                <a href="<%= request.getContextPath()%>/admin/home.jsp">Trang chu</a> |
                <a href="<%= request.getContextPath()%>/LogoutServlet">Dang xuat</a>
            </p>


            <%
                String error = (String) request.getAttribute("error");
                if (error != null) {
            %>
            <p class="error"><%= error%></p>
            <%
                }
                String keyword = (String) request.getAttribute("keyword");
                if (keyword == null) {
                    keyword = "";
                }
                String roleFilter = (String) request.getAttribute("roleFilter");
                if (roleFilter == null) {
                    roleFilter = "";
                }
            %>

            <form action="UserServlet" method="get">
                <input type="hidden" name="action" value="search"/>
                <input type="text" name="keyword" placeholder="Tim theo ten dang nhap hoac ho ten" value="<%= keyword%>"/>
                <select name="role">
                    <option value="" <%= roleFilter.isEmpty() ? "selected" : ""%>>-- Tat ca vai tro --</option>
                    <option value="ADMIN" <%= "ADMIN".equals(roleFilter) ? "selected" : ""%>>Admin</option>
                    <option value="STAFF" <%= "STAFF".equals(roleFilter) ? "selected" : ""%>>Staff</option>
                    <option value="CUSTOMER" <%= "CUSTOMER".equals(roleFilter) ? "selected" : ""%>>Customer</option>
                </select>
                <button type="submit">Tim kiem</button>
                <a href="UserServlet">Xem tat ca</a>
            </form>
            <div class="table-responsive">
                <table>
                    <tr>
                        <th>ID</th><th>Ten dang nhap</th><th>Ho ten</th><th>Email</th>
                        <th>SDT</th><th>Vai tro</th><th>Trang thai</th><th>Hanh dong</th>
                    </tr>
                    <%
                        List<User> users = (List<User>) request.getAttribute("users");
                        if (users != null) {
                            for (User u : users) {
                    %>
                    <tr id="tr_User_<%= u.getUserID()%>">
                        <td id="td_UserID_<%= u.getUserID()%>"><%= u.getUserID()%></td>
                        <td id="td_Username_<%= u.getUserID()%>"><%= u.getUsername()%></td>
                        <td id="td_FullName_<%= u.getUserID()%>"><%= u.getFullName()%></td>
                        <td id="td_Email_<%= u.getUserID()%>"><%= (u.getEmail() == null) ? "" : u.getEmail()%></td>
                        <td id="td_Phone_<%= u.getUserID()%>"><%= (u.getPhone() == null) ? "" : u.getPhone()%></td>
                        <td id="td_Role_<%= u.getUserID()%>"><%= u.getRole()%></td>
                        <td id="td_Status_<%= u.getUserID()%>"><%= u.isStatus() ? "Dang hoat dong" : "Da khoa"%></td>
                        <td>
                            <a href="UserServlet?action=showEdit&id=<%= u.getUserID()%>">Sua</a> |
                            <form action="UserServlet" method="post" style="display:inline"
                                  onsubmit="return confirm('Xoa tai khoan nay?');">
                                <input type="hidden" name="action" value="delete"/>
                                <input type="hidden" name="id" value="<%= u.getUserID()%>"/>
                                <button type="submit">Xoa</button>
                            </form>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                </table>
            </div>
            <%
        Integer currentPage = (Integer) request.getAttribute("currentPage");
        Integer totalPages = (Integer) request.getAttribute("totalPages");
        if (currentPage == null) currentPage = 1;
        if (totalPages == null) totalPages = 1;
        String pageKeyword = (keyword == null) ? "" : keyword;
        String pageRole = (roleFilter == null) ? "" : roleFilter;
            %>
            <div>
                <% if (currentPage > 1) { %>
                <a href="UserServlet?action=search&keyword=<%= pageKeyword%>&role=<%= pageRole%>&page=<%= currentPage - 1%>">Previous</a>
                <% }
                    for (int p = 1; p <= totalPages; p++) {
                        if (p == currentPage) {
                %>
                <b> <%= p%> </b>
                <%
                    } else {
                %>
                <a href="UserServlet?action=search&keyword=<%= pageKeyword%>&role=<%= pageRole%>&page=<%= p%>"> <%= p%> </a>
                <%
                        }
                    }
                    if (currentPage < totalPages) {
                %>
                <a href="UserServlet?action=search&keyword=<%= pageKeyword%>&role=<%= pageRole%>&page=<%= currentPage + 1%>">Next</a>
                <% }%>
            </div>

            <%
                User editUser = (User) request.getAttribute("editUser");

                String fUsername = (request.getAttribute("formUsername") != null)
                        ? (String) request.getAttribute("formUsername")
                        : (editUser != null ? editUser.getUsername() : "");
                String fFullName = (request.getAttribute("formFullName") != null)
                        ? (String) request.getAttribute("formFullName")
                        : (editUser != null ? editUser.getFullName() : "");
                String fEmail = (request.getAttribute("formEmail") != null)
                        ? (String) request.getAttribute("formEmail")
                        : (editUser != null ? editUser.getEmail() : "");
                String fPhone = (request.getAttribute("formPhone") != null)
                        ? (String) request.getAttribute("formPhone")
                        : (editUser != null ? editUser.getPhone() : "");
                String fRole = (request.getAttribute("formRole") != null)
                        ? (String) request.getAttribute("formRole")
                        : (editUser != null ? editUser.getRole() : "STAFF");
                Boolean fStatusObj = (Boolean) request.getAttribute("formStatus");
                boolean fStatus = (fStatusObj != null) ? fStatusObj
                        : (editUser != null ? editUser.isStatus() : true);
                String fUserID = (request.getAttribute("formUserID") != null)
                        ? (String) request.getAttribute("formUserID")
                        : (editUser != null ? String.valueOf(editUser.getUserID()) : null);
                boolean isEditMode = (fUserID != null);
            %>
            <h3><%= isEditMode ? "Cap Nhat Tai Khoan" : "Them Tai Khoan Moi"%></h3>
            <form action="UserServlet" method="post">
                <input type="hidden" name="action" value="<%= isEditMode ? "update" : "add"%>"/>
                <% if (isEditMode) {%>
                <input type="hidden" name="userID" value="<%= fUserID%>"/>
                <% }%>

                <label>Ten dang nhap:</label>
                <% if (isEditMode) {%>
                <input type="text" value="<%= fUsername%>" readonly/>
                <% } else {%>
                <input type="text" name="username" value="<%= fUsername%>"/>
                <% }%><br/><br/>

                <% if (!isEditMode) {%>
                <label>Mat khau:</label>
                <input type="password" name="password"/><br/><br/>
                <% }%>

                <label>Ho ten:</label>
                <input type="text" name="fullName" value="<%= (fFullName == null) ? "" : fFullName%>"/><br/><br/>

                <label>Email:</label>
                <input type="text" name="email" value="<%= (fEmail == null) ? "" : fEmail%>"/><br/><br/>

                <label>So dien thoai:</label>
                <input type="text" name="phone" value="<%= (fPhone == null) ? "" : fPhone%>"/><br/><br/>

                <label>Vai tro:</label>
                <select name="role">
                    <option value="ADMIN" <%= "ADMIN".equals(fRole) ? "selected" : ""%>>Admin</option>
                    <option value="STAFF" <%= "STAFF".equals(fRole) ? "selected" : ""%>>Staff</option>
                    <option value="CUSTOMER" <%= "CUSTOMER".equals(fRole) ? "selected" : ""%>>Customer</option>
                </select><br/><br/>

                <label>
                    <input type="checkbox" name="status" <%= fStatus ? "checked" : ""%>/>
                    Dang hoat dong (bo chon de khoa tai khoan)
                </label><br/><br/>

                <button type="submit"><%= isEditMode ? "Cap nhat" : "Them moi"%></button>
                <% if (isEditMode) {%>
                <a href="UserServlet">Huy</a>
                <% }%>
            </form>

            <% if (isEditMode) {%>
            <h3>Dat lai mat khau</h3>
            <form action="UserServlet" method="post">
                <input type="hidden" name="action" value="resetPassword"/>
                <input type="hidden" name="userID" value="<%= fUserID%>"/>
                <label>Mat khau moi:</label>
                <input type="password" name="newPassword" required/>
                <button type="submit">Dat lai mat khau</button>
            </form>
            <% }%>
        </div>
    </body>
</html>