<!DOCTYPE html>
<!--
Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Html.html to edit this template
-->
<html>
    <head>
        <title>Dang nhap - He thong Quan Ly Bai Gui Xe</title>
        <style>
            *{
                margin:0;
                padding:0;
                box-sizing:border-box;
                font-family:Arial, Helvetica, sans-serif;
            }

            body{
                min-height:100vh;
                display:flex;
                justify-content:center;
                align-items:center;
                padding:20px;
                background:linear-gradient(135deg,#0f172a,#1e40af,#3b82f6);
                overflow:hidden;
            }

            /* Bong nen */
            body::before{
                content:"";
                position:absolute;
                width:450px;
                height:450px;
                background:rgba(255,255,255,.08);
                border-radius:50%;
                top:-120px;
                left:-120px;
            }

            body::after{
                content:"";
                position:absolute;
                width:350px;
                height:350px;
                background:rgba(255,255,255,.05);
                border-radius:50%;
                bottom:-100px;
                right:-100px;
            }

            .login-box{
                position:relative;
                z-index:10;
                width:100%;
                max-width:420px;
                background:rgba(255,255,255,.96);
                backdrop-filter:blur(15px);
                padding:40px;
                border-radius:20px;
                box-shadow:0 15px 40px rgba(0,0,0,.35);
                animation:fadeIn .8s ease;
            }

            @keyframes fadeIn{
                from{
                    opacity:0;
                    transform:translateY(40px);
                }
                to{
                    opacity:1;
                    transform:translateY(0);
                }
            }

            .welcome{
                text-align:center;
                color:#1e3a8a;
                font-size:25px;
                font-weight:bold;
                margin-bottom:8px;
            }

            .subtitle{
                text-align:center;
                color:#666;
                margin-bottom:28px;
                font-size:14px;
            }

            .login-box h2{
                text-align:center;
                color:#2563eb;
                margin-bottom:25px;
                font-size:30px;
            }

            label{
                display:block;
                margin-bottom:6px;
                font-weight:bold;
                color:#444;
            }

            .login-box input[type="text"],
            .login-box input[type="password"]{
                width:100%;
                padding:13px;
                border:1px solid #d1d5db;
                border-radius:10px;
                font-size:15px;
                margin-bottom:18px;
                transition:.3s;
            }

            .login-box input:focus{
                outline:none;
                border-color:#2563eb;
                box-shadow:0 0 10px rgba(37,99,235,.3);
            }

            button{
                width:100%;
                padding:14px;
                border:none;
                border-radius:10px;
                background:linear-gradient(90deg,#2563eb,#1d4ed8);
                color:white;
                font-size:16px;
                font-weight:bold;
                cursor:pointer;
                transition:.3s;
            }

            button:hover{
                transform:translateY(-3px);
                box-shadow:0 10px 20px rgba(37,99,235,.35);
            }

            button:active{
                transform:scale(.98);
            }

            .error{
                background:#ffe5e5;
                color:#c00000;
                border-left:5px solid red;
                padding:12px;
                margin-bottom:18px;
                border-radius:8px;
            }

            .login-box p{
                text-align:center;
                margin-top:20px;
            }

            .login-box a{
                color:#2563eb;
                text-decoration:none;
                font-weight:bold;
                transition:.3s;
            }

            .login-box a:hover{
                color:#1d4ed8;
                text-decoration:underline;
            }

            /* Tablet */
            @media(max-width:768px){

                .login-box{
                    max-width:430px;
                    padding:35px;
                }

                .welcome{
                    font-size:22px;
                }

                .login-box h2{
                    font-size:27px;
                }
            }

            /* Mobile */
            @media(max-width:480px){

                body{
                    padding:15px;
                }

                .login-box{
                    padding:28px 22px;
                    border-radius:15px;
                }

                .welcome{
                    font-size:20px;
                }

                .subtitle{
                    font-size:13px;
                }

                .login-box h2{
                    font-size:24px;
                }

                button{
                    padding:13px;
                }
            }
        </style>
    </head>
    <body>
        <div class="login-box">
            <div class="welcome">
                Chao mung den voi He thong Quan Ly Bai Do Xe
            </div>

            <div class="subtitle">
                Dang nhap de tiep tuc su dung he thong
            </div>
            <h2>Dang nhap</h2>
            <%
            String error = (String) request.getAttribute("error");
            if (error != null) {
            %>
            <p class="error"><%= error%></p>
            <%
    if ("1".equals(request.getParameter("registered"))) {
            %>
            <p style="color: green;">Dang ky thanh cong! Vui long dang nhap.</p>
            <%
                }
            %>
            <%
            }
            %>
            <form action="LoginServlet" method="post">
                <label>Ten dang nhap:</label><br/>
                <input type="text" name="username" required/><br/>
                <label>Mat khau:</label><br/>
                <input type="password" name="password" required/><br/><br/>
                <button type="submit">Dang nhap</button>
            </form>
            <p><a href="RegisterServlet">Chua co tai khoan? Dang ky ngay</a></p>
        </div>
    </body>
</html>
