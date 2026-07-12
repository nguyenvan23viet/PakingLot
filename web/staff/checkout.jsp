<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="model.Vehicle"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Check-out Xe</title>
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
            <h2>Check-out Xe</h2>
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
            <input type="hidden" name="action" value="showCheckOut"/>
            <label>Nhap bien so xe:</label>
            <input type="text" name="plate" value="<%= searchedPlate%>" required/>
            <button type="submit">Tim xe dang gui</button>
        </form>

        <%
            Vehicle previewVehicle = (Vehicle) request.getAttribute("previewVehicle");
            if (previewVehicle != null) {
                Integer previewTicketID = (Integer) request.getAttribute("previewTicketID");
                String previewCheckIn = (String) request.getAttribute("previewCheckIn");
                String previewRateDesc = (String) request.getAttribute("previewRateDesc");
                Double previewFee = (Double) request.getAttribute("previewFee");
        %>
        <h3>Thong tin ve gui xe</h3>
        <p>Bien so: <b><%= previewVehicle.getLicensePlate()%></b> - Loai xe: <%= previewVehicle.getVehicleType()%></p>
        <p>Ma ve: #<%= previewTicketID%></p>
        <p>Gio vao: <%= previewCheckIn%></p>
        <p>Hinh thuc tinh phi: <%= previewRateDesc%></p>
        <p><b>Tam tinh phi (den thoi diem hien tai): <%= previewFee%> VND</b></p>

        <form action="TicketServlet" method="post">
            <input type="hidden" name="action" value="checkout"/>
            <input type="hidden" name="ticketID" value="<%= previewTicketID%>"/>
            <button type="submit">Xac nhan Check-out</button>
        </form>
        <%
            }
        %>
        </div>
    </body>
</html>