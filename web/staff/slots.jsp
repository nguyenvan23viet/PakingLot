<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="java.util.List"%>
<%@page import="model.ParkingSlot"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Vi Tri Do Xe</title>
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
            <h2>Vi Tri Do Xe</h2>
            <p>
                <a href="<%= request.getContextPath()%>/staff/home.jsp">Trang chu</a> |
                <a href="<%= request.getContextPath()%>/LogoutServlet">Dang xuat</a>
            </p>


            <%
                String keyword = (String) request.getAttribute("keyword");
                if (keyword == null) {
                    keyword = "";
                }
                String statusFilter = (String) request.getAttribute("status");
                if (statusFilter == null) {
                    statusFilter = "";
                }
            %>
            <form action="ParkingSlotServlet" method="get">
                <input type="hidden" name="action" value="search"/>
                <input type="text" name="keyword" placeholder="Tim theo ma vi tri (VD: A01)" value="<%= keyword%>"/>
                <select name="status">
                    <option value="" <%= statusFilter.isEmpty() ? "selected" : ""%>>-- Tat ca trang thai --</option>
                    <option value="EMPTY" <%= "EMPTY".equals(statusFilter) ? "selected" : ""%>>Trong</option>
                    <option value="OCCUPIED" <%= "OCCUPIED".equals(statusFilter) ? "selected" : ""%>>Da co xe</option>
                </select>
                <button type="submit">Tim kiem</button>
                <a href="ParkingSlotServlet">Xem tat ca</a>
            </form>
            <div class="table-responsive">
                <table>
                    <tr>
                        <th>Ma vi tri</th>
                        <th>Ma code</th>
                        <th>Loai xe</th>
                        <th>Trang thai</th>
                    </tr>
                    <%
                        List<ParkingSlot> slots = (List<ParkingSlot>) request.getAttribute("slots");
                        if (slots != null) {
                            for (ParkingSlot s : slots) {
                    %>
                    <tr id="tr_Slot_<%= s.getSlotID()%>">
                        <td id="td_SlotID_<%= s.getSlotID()%>"><%= s.getSlotID()%></td>
                        <td id="td_SlotCode_<%= s.getSlotID()%>"><%= s.getSlotCode()%></td>
                        <td id="td_SlotType_<%= s.getSlotID()%>"><%= s.getSlotType()%></td>
                        <td id="td_Status_<%= s.getSlotID()%>"><%= s.getStatus()%></td>
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
        String pageStatus = (statusFilter == null) ? "" : statusFilter;
            %>
            <div>
                <% if (currentPage > 1) { %>
                <a href="ParkingSlotServlet?action=search&keyword=<%= pageKeyword%>&status=<%= pageStatus%>&page=<%= currentPage - 1%>">Previous</a>
                <% }
                    for (int p = 1; p <= totalPages; p++) {
                        if (p == currentPage) {
                %>
                <b> <%= p%> </b>
                <%
                    } else {
                %>
                <a href="ParkingSlotServlet?action=search&keyword=<%= pageKeyword%>&status=<%= pageStatus%>&page=<%= p%>"> <%= p%> </a>
                <%
                        }
                    }
                    if (currentPage < totalPages) {
                %>
                <a href="ParkingSlotServlet?action=search&keyword=<%= pageKeyword%>&status=<%= pageStatus%>&page=<%= currentPage + 1%>">Next</a>
                <% }%>
            </div>
        </div>
    </body>
</html>
