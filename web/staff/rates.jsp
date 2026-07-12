<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="java.util.List"%>
<%@page import="model.ParkingRate"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Bang Gia Gui Xe</title>
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
            <h2>Bang Gia Gui Xe</h2>
            <p>
                <a href="<%= request.getContextPath()%>/staff/home.jsp">Trang chu</a> |
                <a href="<%= request.getContextPath()%>/LogoutServlet">Dang xuat</a>
            </p>


            <%
                String keyword = (String) request.getAttribute("keyword");
                if (keyword == null) {
                    keyword = "";
                }
            %>
            <form action="RateServlet" method="get">
                <input type="hidden" name="action" value="search"/>
                <input type="text" name="keyword" placeholder="Tim theo loai xe (MOTORBIKE/CAR)" value="<%= keyword%>"/>
                <button type="submit">Tim kiem</button>
                <a href="RateServlet">Xem tat ca</a>
            </form>
            <div class="table-responsive">
                <table>
                    <tr>
                        <th>Ma gia</th>
                        <th>Loai xe</th>
                        <th>Hinh thuc</th>
                        <th>Don gia (VND)</th>
                        <th>Mo ta</th>
                    </tr>
                    <%
                        List<ParkingRate> rates = (List<ParkingRate>) request.getAttribute("rates");
                        if (rates != null) {
                            for (ParkingRate r : rates) {
                    %>
                    <tr id="tr_Rate_<%= r.getRateID()%>">
                        <td id="td_RateID_<%= r.getRateID()%>"><%= r.getRateID()%></td>
                        <td id="td_VehicleType_<%= r.getRateID()%>"><%= r.getVehicleType()%></td>
                        <td id="td_RateType_<%= r.getRateID()%>"><%= r.getRateType()%></td>
                        <td id="td_Price_<%= r.getRateID()%>"><%= r.getPrice()%></td>
                        <td id="td_Description_<%= r.getRateID()%>"><%= r.getDescription()%></td>
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
                <a href="RateServlet?action=search&keyword=<%= pageKeyword%>&page=<%= currentPage - 1%>">Previous</a>
                <% }
                    for (int p = 1; p <= totalPages; p++) {
                        if (p == currentPage) {
                %>
                <b> <%= p%> </b>
                <%
                    } else {
                %>
                <a href="RateServlet?action=search&keyword=<%= pageKeyword%>&page=<%= p%>"> <%= p%> </a>
                <%
                        }
                    }
                    if (currentPage < totalPages) {
                %>
                <a href="RateServlet?action=search&keyword=<%= pageKeyword%>&page=<%= currentPage + 1%>">Next</a>
                <% }%>
            </div>
        </div>
    </body>
</html>
