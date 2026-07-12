<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="java.util.List"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Lich Su Gui Xe</title>
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
            <h2>Lich Su Gui Xe Cua Toi</h2>
            <p>
                <a href="<%= request.getContextPath()%>/customer/home.jsp">Trang chu</a> |
                <a href="<%= request.getContextPath()%>/LogoutServlet">Dang xuat</a>
            </p>


            <%
                String status = (String) request.getAttribute("status");
                if (status == null) {
                    status = "";
                }
                String fromDate = (String) request.getAttribute("fromDate");
                if (fromDate == null) {
                    fromDate = "";
                }
                String toDate = (String) request.getAttribute("toDate");
                if (toDate == null) {
                    toDate = "";
                }
            %>

            <form action="TicketServlet" method="get">
                <input type="hidden" name="action" value="search"/>
                <select name="status">
                    <option value="" <%= status.isEmpty() ? "selected" : ""%>>-- Tat ca trang thai --</option>
                    <option value="PARKING" <%= "PARKING".equals(status) ? "selected" : ""%>>Dang gui</option>
                    <option value="COMPLETED" <%= "COMPLETED".equals(status) ? "selected" : ""%>>Da hoan tat</option>
                </select>
                Tu ngay: <input type="date" name="fromDate" value="<%= fromDate%>"/>
                Den ngay: <input type="date" name="toDate" value="<%= toDate%>"/>
                <button type="submit">Tim kiem</button>
                <a href="TicketServlet">Xem tat ca</a>
            </form>
            <div class="table-responsive">
                <table>
                    <tr>
                        <th>Ma ve</th><th>Bien so</th><th>Loai xe</th><th>Vi tri</th>
                        <th>Gio vao</th><th>Gio ra</th><th>Phi (VND)</th><th>Trang thai</th>
                    </tr>
                    <%
                        List<String[]> rows = (List<String[]>) request.getAttribute("rows");
                        if (rows != null) {
                            for (String[] r : rows) {
                    %>
                    <tr id="tr_Ticket_<%= r[0]%>">
                        <td id="td_TicketID_<%= r[0]%>"><%= r[0]%></td>
                        <td id="td_Plate_<%= r[0]%>"><%= r[1]%></td>
                        <td id="td_VehicleType_<%= r[0]%>"><%= r[2]%></td>
                        <td id="td_Slot_<%= r[0]%>"><%= r[3]%></td>
                        <td id="td_CheckIn_<%= r[0]%>"><%= r[4]%></td>
                        <td id="td_CheckOut_<%= r[0]%>"><%= r[5]%></td>
                        <td id="td_Fee_<%= r[0]%>"><%= r[6]%></td>
                        <td id="td_Status_<%= r[0]%>"><%= r[7]%></td>
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
        String pageStatus = (status == null) ? "" : status;
        String pageFrom = (fromDate == null) ? "" : fromDate;
        String pageTo = (toDate == null) ? "" : toDate;
        String pageQS = "action=search&status=" + pageStatus + "&fromDate=" + pageFrom + "&toDate=" + pageTo;
            %>
            <div>
                <% if (currentPage > 1) { %>
                <a href="TicketServlet?<%= pageQS%>&page=<%= currentPage - 1%>">Previous</a>
                <% }
                    for (int p = 1; p <= totalPages; p++) {
                        if (p == currentPage) {
                %>
                <b> <%= p%> </b>
                <%
                    } else {
                %>
                <a href="TicketServlet?<%= pageQS%>&page=<%= p%>"> <%= p%> </a>
                <%
                        }
                    }
                    if (currentPage < totalPages) {
                %>
                <a href="TicketServlet?<%= pageQS%>&page=<%= currentPage + 1%>">Next</a>
                <% }%>
            </div>
        </div>
    </body>
</html>