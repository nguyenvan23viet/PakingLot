<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="java.util.List"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Bao Cao Thong Ke</title>
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

            <h2>Bao Cao Thong Ke</h2>
            <p>
                <a href="<%= request.getContextPath()%>/admin/home.jsp">Trang chu</a> |
                <a href="<%= request.getContextPath()%>/LogoutServlet">Dang xuat</a>
            </p>


            <%
                String fromDate = (String) request.getAttribute("fromDate");
                String toDate = (String) request.getAttribute("toDate");
            %>
            <form action="ReportServlet" method="get">
                Tu ngay: <input type="date" name="fromDate" value="<%= fromDate%>"/>
                Den ngay: <input type="date" name="toDate" value="<%= toDate%>"/>
                <button type="submit">Loc theo khoang ngay</button>
            </form>
            <p><i>(Rieng bao cao "Ty le lap day bai xe" luon la so lieu hien tai, khong theo khoang ngay loc.)</i></p>

            <!-- Bao cao 1: Doanh thu theo ngay -->
            <h3>1. Doanh Thu Theo Ngay</h3>
            <div class="table-responsive">
                <table>
                    <tr><th>Ngay</th><th>Doanh thu (VND)</th></tr>
                            <%
                                List<Object[]> revenueByDate = (List<Object[]>) request.getAttribute("revenueByDate");
                                if (revenueByDate != null && !revenueByDate.isEmpty()) {
                                    for (Object[] row : revenueByDate) {
                            %>
                    <tr>
                        <td><%= row[0]%></td>
                        <td><%= row[1]%></td>
                    </tr>
                    <%
                            }
                        } else {
                    %>
                    <tr><td colspan="2">Khong co du lieu trong khoang ngay nay.</td></tr>
                    <%
                        }
                    %>
                </table>

                <!-- Bao cao 2: Luot xe vao/ra theo ngay -->
                <h3>2. Luot Xe Vao/Ra Theo Ngay</h3>
                <table>
                    <tr><th>Ngay</th><th>So luot Check-in</th><th>So luot Check-out</th></tr>
                            <%
                                List<String[]> trafficRows = (List<String[]>) request.getAttribute("trafficRows");
                                if (trafficRows != null && !trafficRows.isEmpty()) {
                                    for (String[] row : trafficRows) {
                            %>
                    <tr>
                        <td><%= row[0]%></td>
                        <td><%= row[1]%></td>
                        <td><%= row[2]%></td>
                    </tr>
                    <%
                            }
                        } else {
                    %>
                    <tr><td colspan="3">Khong co du lieu trong khoang ngay nay.</td></tr>
                    <%
                        }
                    %>
                </table>

                <!-- Bao cao 3: Ty le lap day bai xe -->
                <h3>3. Ty Le Lap Day Bai Xe (hien tai)</h3>
                <table>
                    <tr><th>Loai xe</th><th>Tong so vi tri</th><th>Da co xe</th><th>Ty le lap day</th></tr>
                            <%
                                List<Object[]> slotOccupancy = (List<Object[]>) request.getAttribute("slotOccupancy");
                                if (slotOccupancy != null && !slotOccupancy.isEmpty()) {
                                    for (Object[] row : slotOccupancy) {
                                        int total = (Integer) row[1];
                                        int occupied = (Integer) row[2];
                                        double percent = (total == 0) ? 0 : (occupied * 100.0 / total);
                            %>
                    <tr>
                        <td><%= row[0]%></td>
                        <td><%= total%></td>
                        <td><%= occupied%></td>
                        <td><%= String.format("%.1f", percent)%>%</td>
                    </tr>
                    <%
                            }
                        } else {
                    %>
                    <tr><td colspan="4">Chua co vi tri do xe nao trong he thong.</td></tr>
                    <%
                        }
                    %>
                </table>

                <!-- Bao cao 4: Top 5 khach hang -->
                <h3>4. Top 5 Khach Hang Gui Xe Nhieu Nhat</h3>
                <table>
                    <tr><th>Ten khach hang</th><th>So lan gui xe</th><th>Tong chi tieu (VND)</th></tr>
                            <%
                                List<Object[]> topCustomers = (List<Object[]>) request.getAttribute("topCustomers");
                                if (topCustomers != null && !topCustomers.isEmpty()) {
                                    for (Object[] row : topCustomers) {
                            %>
                    <tr>
                        <td><%= row[0]%></td>
                        <td><%= row[1]%></td>
                        <td><%= row[2]%></td>
                    </tr>
                    <%
                            }
                        } else {
                    %>
                    <tr><td colspan="3">Khong co du lieu trong khoang ngay nay.</td></tr>
                    <%
                        }
                    %>
                </table>

                <!-- Bao cao 5: Doanh thu theo loai xe -->
                <h3>5. Doanh Thu Theo Loai Xe</h3>
                <table>
                    <tr><th>Loai xe</th><th>Doanh thu (VND)</th><th>So luot gui</th></tr>
                            <%
                                List<Object[]> revenueByVehicleType = (List<Object[]>) request.getAttribute("revenueByVehicleType");
                                if (revenueByVehicleType != null && !revenueByVehicleType.isEmpty()) {
                                    for (Object[] row : revenueByVehicleType) {
                            %>
                    <tr>
                        <td><%= row[0]%></td>
                        <td><%= row[1]%></td>
                        <td><%= row[2]%></td>
                    </tr>
                    <%
                            }
                        } else {
                    %>
                    <tr><td colspan="3">Khong co du lieu trong khoang ngay nay.</td></tr>
                    <%
                        }
                    %>
                </table>
            </div>
        </div>
    </body>
</html>