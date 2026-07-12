<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="java.util.List"%>
<%@page import="model.Vehicle"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Xe cua toi</title>
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
            <h2>Xe cua toi</h2>
            <p>
                <a href="<%= request.getContextPath()%>/customer/home.jsp">Trang chu</a> |
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
            %>

            <form action="VehicleServlet" method="get">
                <input type="hidden" name="action" value="search"/>
                <input type="text" name="keyword" placeholder="Tim theo bien so xe cua ban" value="<%= keyword%>"/>
                <button type="submit">Tim kiem</button>
                <a href="VehicleServlet">Xem tat ca</a>
            </form>
            <div class="table-responsive">
                <table>
                    <tr>
                        <th>Ma xe</th>
                        <th>Bien so</th>
                        <th>Loai xe</th>
                        <th>Hang xe</th>
                        <th>Mau</th>
                        <th>Hanh dong</th>
                    </tr>
                    <%
                        List<Vehicle> vehicles = (List<Vehicle>) request.getAttribute("vehicles");
                        if (vehicles != null) {
                            for (Vehicle v : vehicles) {
                    %>
                    <tr id="tr_Vehicle_<%= v.getVehicleID()%>">
                        <td id="td_VehicleID_<%= v.getVehicleID()%>"><%= v.getVehicleID()%></td>
                        <td id="td_LicensePlate_<%= v.getVehicleID()%>"><%= v.getLicensePlate()%></td>
                        <td id="td_VehicleType_<%= v.getVehicleID()%>"><%= v.getVehicleType()%></td>
                        <td id="td_Brand_<%= v.getVehicleID()%>"><%= (v.getBrand() == null) ? "" : v.getBrand()%></td>
                        <td id="td_Color_<%= v.getVehicleID()%>"><%= (v.getColor() == null) ? "" : v.getColor()%></td>
                        <td>
                            <a href="VehicleServlet?action=showEdit&id=<%= v.getVehicleID()%>">Sua</a>
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
            %>
            <div>
                <% if (currentPage > 1) { %>
                <a href="VehicleServlet?action=search&keyword=<%= pageKeyword%>&page=<%= currentPage - 1%>">Previous</a>
                <% }
                    for (int p = 1; p <= totalPages; p++) {
                        if (p == currentPage) {
                %>
                <b> <%= p%> </b>
                <%
                    } else {
                %>
                <a href="VehicleServlet?action=search&keyword=<%= pageKeyword%>&page=<%= p%>"> <%= p%> </a>
                <%
                        }
                    }
                    if (currentPage < totalPages) {
                %>
                <a href="VehicleServlet?action=search&keyword=<%= pageKeyword%>&page=<%= currentPage + 1%>">Next</a>
                <% }%>
            </div>

            <%
                Vehicle editVehicle = (Vehicle) request.getAttribute("editVehicle");

                String fLicensePlate = (request.getAttribute("formLicensePlate") != null)
                        ? (String) request.getAttribute("formLicensePlate")
                        : (editVehicle != null ? editVehicle.getLicensePlate() : "");
                String fVehicleType = (request.getAttribute("formVehicleType") != null)
                        ? (String) request.getAttribute("formVehicleType")
                        : (editVehicle != null ? editVehicle.getVehicleType() : "");
                String fBrand = (request.getAttribute("formBrand") != null)
                        ? (String) request.getAttribute("formBrand")
                        : (editVehicle != null ? editVehicle.getBrand() : "");
                String fColor = (request.getAttribute("formColor") != null)
                        ? (String) request.getAttribute("formColor")
                        : (editVehicle != null ? editVehicle.getColor() : "");
                String fVehicleID = (request.getAttribute("formVehicleID") != null)
                        ? (String) request.getAttribute("formVehicleID")
                        : (editVehicle != null ? String.valueOf(editVehicle.getVehicleID()) : null);
                boolean isEditMode = (fVehicleID != null);
            %>
            <h3><%= isEditMode ? "Cap Nhat Xe" : "Dang Ky Xe Moi"%></h3>
            <form action="VehicleServlet" method="post">
                <input type="hidden" name="action" value="<%= isEditMode ? "update" : "add"%>"/>
                <% if (isEditMode) {%>
                <input type="hidden" name="vehicleID" value="<%= fVehicleID%>"/>
                <% }%>

                <label>Bien so xe:</label>
                <input type="text" name="licensePlate" value="<%= (fLicensePlate == null) ? "" : fLicensePlate%>"
                       <%= isEditMode ? "readonly" : ""%>/><br/><br/>

                <label>Loai xe:</label>
                <select name="vehicleType">
                    <option value="MOTORBIKE" <%= "MOTORBIKE".equals(fVehicleType) ? "selected" : ""%>>Xe may</option>
                    <option value="CAR" <%= "CAR".equals(fVehicleType) ? "selected" : ""%>>O to</option>
                </select><br/><br/>

                <label>Hang xe:</label>
                <input type="text" name="brand" value="<%= (fBrand == null) ? "" : fBrand%>"/><br/><br/>

                <label>Mau xe:</label>
                <input type="text" name="color" value="<%= (fColor == null) ? "" : fColor%>"/><br/><br/>

                <button type="submit"><%= isEditMode ? "Cap nhat" : "Dang ky"%></button>
                <% if (isEditMode) {%>
                <a href="VehicleServlet">Huy</a>
                <% }%>
            </form>
        </div>
    </body>
</html>
