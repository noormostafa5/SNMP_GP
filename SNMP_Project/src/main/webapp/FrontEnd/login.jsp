<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>SNMP Login</title>
    <link rel="stylesheet" type="text/css" href="Styles/style.css">
</head>
<body>
    <div id="login-page" class="login-container">
        <div class="login-box">
            <h2 class="login-title">SNMP Platform Login</h2>
            <input type="text" id="firstName" class="input" placeholder="First Name" autocomplete="off" />
            <input type="text" id="lastName" class="input" placeholder="Last Name" autocomplete="off" />
            <input type="password" id="password" class="input" placeholder="Password" autocomplete="off" />
            <button class="button">Login</button>
            <p id="login-error" class="error-text"></p>
        </div>
    </div>
</body>
</html>
