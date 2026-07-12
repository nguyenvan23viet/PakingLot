<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="model.Vehicle"%>
<%@page import="model.ParkingSlot"%>
<%@page import="java.util.List"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Check-in Xe</title>
                <style>
            * { box-sizing: border-box; }
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
            th { background: #f1f5f9; }
            .error { color: red; }

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
            <h2>Check-in Xe</h2>
            <p>
                <a href="<%= request.getContextPath()%>/staff/home.jsp">Trang chu</a> |
                <a href="<%= request.getContextPath()%>/LogoutServlet">Dang xuat</a>
            </p>
        

        <%
            String error = (String) request.getAttribute("error");
            if (error != null) {
        %>
        <p class="error"><%= error%></p>
        <%
            }
            String message = (String) request.getAttribute("message");
            if (message != null) {
        %>
        <p class="message"><%= message%></p>
        <%
            }
            String searchedPlate = (String) request.getAttribute("searchedPlate");
            if (searchedPlate == null) {
                searchedPlate = "";
            }
        %>

        <form action="TicketServlet" method="get">
            <input type="hidden" name="action" value="showCheckIn"/>
            <label>Nhap bien so xe:</label>
            <input type="text" name="plate" value="<%= searchedPlate%>" required/>
            <button type="submit">Tim xe</button>
        </form>

        <%
            Vehicle foundVehicle = (Vehicle) request.getAttribute("foundVehicle");
            List<ParkingSlot> availableSlots = (List<ParkingSlot>) request.getAttribute("availableSlots");
            if (foundVehicle != null) {
        %>
        <h3>Thong tin xe</h3>
        <p>
            Bien so: <b><%= foundVehicle.getLicensePlate()%></b> -
            Loai xe: <%= foundVehicle.getVehicleType()%> -
            Hang xe: <%= (foundVehicle.getBrand() == null) ? "" : foundVehicle.getBrand()%>
        </p>

        <% if (availableSlots == null || availableSlots.isEmpty()) {%>
        <p class="error">Khong con vi tri trong cho loai xe nay.</p>
        <% } else {%>
        <form action="TicketServlet" method="post">
            <input type="hidden" name="action" value="checkin"/>
            <input type="hidden" name="plate" value="<%= foundVehicle.getLicensePlate()%>"/>

            <label>Chon vi tri do (con trong):</label>
            <select name="slotID">
                <%
                    for (ParkingSlot s : availableSlots) {
                %>
                <option value="<%= s.getSlotID()%>"><%= s.getSlotCode()%></option>
                <%
                    }
                %>
            </select><br/><br/>

            <label>Hinh thuc tinh phi:</label>
            <select name="rateType">
                <option value="HOURLY">Theo gio</option>
                <option value="MONTHLY">Theo thang</option>
            </select><br/><br/>

            <button type="submit">Xac nhan Check-in</button>
        </form>
        <% }%>
        <%
            }
        %>
        </div>
    </body>
</html>