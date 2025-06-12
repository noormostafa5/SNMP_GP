<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>SNMP Login</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/FrontEnd/Styles/style.css?v=<%= System.currentTimeMillis() %>">
</head>
<body style="background: url('${pageContext.request.contextPath}/FrontEnd/background/bckg1.jpg') no-repeat center center fixed; background-size: cover;">
    <div id="login-page" class="login-container">
        <div class="login-box">
            <h2 class="login-title">SNMP Platform Login</h2>
            <form method="POST" action="${pageContext.request.contextPath}/login">
                <input type="text" id="firstName" name="firstName" class="input" placeholder="First Name" autocomplete="off" />
                <input type="text" id="lastName" name="lastName" class="input" placeholder="Last Name" autocomplete="off" />
                <input type="password" id="password" name="password" class="input" placeholder="Password" autocomplete="off" />
                <button type="submit" class="button">Login</button>
            </form>
            <p id="login-error" class="error-text" style="color: red;"><%
                if (request.getAttribute("error") != null) {
                    out.print(request.getAttribute("error"));
                }
            %></p>
        </div>
    </div>
</body>
</html>
