<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>SNMP Login</title>
    <!-- ? Fixed the path using contextPath -->
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/FrontEnd/Styles/login.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>
<div class="login-wrapper">
    <div class="login-container">
        <div class="login-header">
            <div class="logo">
                <i class="fas fa-network-wired"></i>
                <span>SNMP Platform</span>
            </div>
            <h1>Welcome Back</h1>
            <p>Sign in to continue to your dashboard</p>
        </div>

        <form class="login-form" action="${pageContext.request.contextPath}/login" method="post" autocomplete="off">
            <div class="form-group">
                <div class="input-icon">
                    <i class="fas fa-user"></i>
                    <input type="text" name="firstName" id="firstName" class="input" placeholder="First Name" autocomplete="off" required />
                </div>
            </div>

            <div class="form-group">
                <div class="input-icon">
                    <i class="fas fa-user"></i>
                    <input type="text" name="lastName" id="lastName" class="input" placeholder="Last Name" autocomplete="off" required />
                </div>
            </div>

            <div class="form-group">
                <div class="input-field">
                    <i class="fas fa-lock"></i>
                    <input type="password" name="password" id="password" class="input" placeholder="Password" autocomplete="off" required />
                    <i class="fas fa-eye toggle-password"></i>
                </div>
            </div>

            <div class="form-options">
                <label class="remember-me">
                    <input type="checkbox" id="remember" />
                    <span>Remember me</span>
                </label>
                <a href="#" class="forgot-password">Forgot Password?</a>
            </div>

            <button type="submit" class="login-button">
                <span>Sign In</span>
                <i class="fas fa-arrow-right"></i>
            </button>

            <% if (request.getAttribute("error") != null) { %>
                <p id="login-error" class="error-text"><%= request.getAttribute("error") %></p>
            <% } else { %>
                <p id="login-error" class="error-text"></p>
            <% } %>
        </form>

        <div class="social-login">
            <p>Or continue with</p>
            <div class="social-buttons">
                <button class="social-button google" onclick="window.open('https://www.geeksforgeeks.org/simple-network-management-protocol-snmp/', '_blank')">
                    <i class="fab fa-google"></i>
                </button>
                <button class="social-button github" onclick="window.open('https://github.com/noormostafa5/SNMP_GP', '_blank')">
                    <i class="fab fa-github"></i>
                </button>
            </div>
        </div>

        <div class="signup-prompt" style="margin-top: -2px;">
            <p>Don't have an account? <a href="${pageContext.request.contextPath}/register">Create Account</a></p>
        </div>
    </div>

    <div class="login-decoration">
        <div class="decoration-content">
            <div class="network-animation">
                <i class="fas fa-circle"></i>
                <i class="fas fa-circle"></i>
                <i class="fas fa-circle"></i>
            </div>
            <h2>Network Monitoring Made Simple</h2>
            <p>Monitor, analyze, and optimize your network infrastructure with our powerful SNMP platform.</p>
            <div class="features">
                <div class="feature">
                    <i class="fas fa-shield-alt"></i>
                    <span>Secure & Reliable</span>
                </div>
                <div class="feature">
                    <i class="fas fa-chart-line"></i>
                    <span>Real-time Analytics</span>
                </div>
                <div class="feature">
                    <i class="fas fa-bell"></i>
                    <span>Smart Alerts</span>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    // Toggle password visibility
    document.querySelector('.toggle-password').addEventListener('click', function() {
        const input = this.previousElementSibling;
        const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
        input.setAttribute('type', type);
        this.classList.toggle('fa-eye');
        this.classList.toggle('fa-eye-slash');
    });
</script>
</body>
</html>
