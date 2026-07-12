<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="model.User"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Trang chu Admin</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <style>
            * { box-sizing: border-box; }
            html, body {
                height: 100%;
                margin: 0;
            }
            body {
                font-family: Arial, sans-serif;
                display: flex;
                flex-direction: column;
            }

            .navbar {
                flex-shrink: 0;
                display: flex;
                align-items: center;
                justify-content: space-between;
                background: #1f2937;
                color: #fff;
                padding: 12px 24px;
                flex-wrap: wrap;
            }
            .navbar .brand {
                font-size: 20px;
                font-weight: bold;
                white-space: nowrap;
            }

            .nav-links {
                display: flex;
                gap: 8px;
                flex-wrap: wrap;
                align-items: center;
            }
            .nav-links a {
                color: #fff;
                text-decoration: none;
                padding: 8px 14px;
                border-radius: 4px;
            }
            .nav-links a:hover {
                background: #374151;
            }
            .nav-links a.logout {
                background: #dc2626;
            }
            .nav-links a.logout:hover {
                background: #b91c1c;
            }

            .nav-select {
                display: none;
                width: 100%;
                padding: 10px;
                font-size: 16px;
                border-radius: 4px;
                border: none;
                margin-top: 10px;
            }

            /* Banner gio chiem HET phan con lai cua man hinh, khong con khoang trang o duoi */
            .banner {
                flex: 1;
                position: relative;
                color: #fff;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                text-align: center;
                padding: 24px;
                background-image:
                    linear-gradient(rgba(15, 23, 42, 0.55), rgba(15, 23, 42, 0.55)),
                    url('https://commons.wikimedia.org/wiki/Special:FilePath/Multi-Level-Stack-Parking-in-NYC.jpg');
                background-size: cover;
                background-position: center;
                background-repeat: no-repeat;
            }
            .banner h1 {
                margin: 0 0 8px 0;
                font-size: 32px;
            }
            .banner p {
                margin: 0;
                font-size: 18px;
                opacity: 0.95;
            }
            .banner .credit {
                position: absolute;
                bottom: 8px;
                right: 12px;
                font-size: 10px;
                opacity: 0.6;
            }

            @media (max-width: 900px) {
                .navbar {
                    flex-direction: column;
                    align-items: stretch;
                }
                .navbar .brand {
                    margin-bottom: 4px;
                }
                .nav-links {
                    display: none;
                }
                .nav-select {
                    display: block;
                }
                .banner h1 {
                    font-size: 24px;
                }
                .banner p {
                    font-size: 15px;
                }
            }
        </style>
    </head>
    <body>
        <%
            User user = (User) session.getAttribute("user");
            String ctx = request.getContextPath();
        %>
        <nav class="navbar">
            <div class="brand">Parking Management</div>

            <div class="nav-links">
                <a href="RateServlet">Bang gia</a>
                <a href="VehicleServlet">Xe</a>
                <a href="ParkingSlotServlet">Vi tri</a>
                <a href="TicketServlet">Ve</a>
                <a href="UserServlet">Tai khoan</a>
                <a href="ReportServlet">Bao cao</a>
                <a class="logout" href="<%= ctx%>/LogoutServlet">Dang xuat</a>
            </div>

            <select class="nav-select" onchange="if(this.value) window.location.href=this.value;">
                <option value="">-- Chon chuc nang --</option>
                <option value="RateServlet">Bang gia</option>
                <option value="VehicleServlet">Xe</option>
                <option value="ParkingSlotServlet">Vi tri</option>
                <option value="TicketServlet">Ve</option>
                <option value="UserServlet">Tai khoan</option>
                <option value="ReportServlet">Bao cao</option>
                <option value="<%= ctx%>/LogoutServlet">Dang xuat</option>
            </select>
        </nav>

        <div class="banner">
            <h1>Xin chao, <%= user.getFullName()%></h1>
            <p>He thong Quan Ly Bai Gui Xe - Vai tro: Admin</p>
            <span class="credit">Anh: Wikimedia Commons</span>
        </div>
    </body>
</html>