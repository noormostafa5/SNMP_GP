<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>SNMP Sign Up</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/FrontEnd/Styles/signUp.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        body {
            background-image: url("${pageContext.request.contextPath}/FrontEnd/background/bckg1.jpg");
            background-size: cover;
            background-position: center;
            background-repeat: no-repeat;
            min-height: 100vh;
            margin: 0;
            padding: 0;
        }
    </style>
</head>
<body>
    <div class="signup-container">
        <div class="signup-content">
            <div class="signup-header">
                <a href="../Pages/welcome.jsp" class="back-button">
                    <i class="fas fa-arrow-left"></i>
                </a>
                <h1>Create Account</h1>
                <p class="subtitle">Join our network monitoring platform</p>
            </div>

            <form class="signup-form" action="${pageContext.request.contextPath}/register" method="post">
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
                    <div class="input-icon">
                        <i class="fas fa-id-card"></i>
                        <input type="text" name="nationalID" id="nationalID" class="input" placeholder="National ID (20 digits)" autocomplete="off" required maxlength="20" pattern="[0-9]{20}" />
                    </div>
                </div>

                <div class="form-group">
                    <div class="input-icon">
                        <i class="fas fa-phone"></i>
                        <input type="tel" name="phoneNumber" id="phoneNumber" class="input" placeholder="Phone Number (optional)" autocomplete="off" />
                    </div>
                </div>

                <div class="form-group">
                    <div class="input-icon">
                        <i class="fas fa-calendar"></i>
                        <input type="date" name="dob" id="dob" class="input" placeholder="Date of Birth (optional)" autocomplete="off" />
                    </div>
                </div>

                <div class="form-group">
                    <div class="input-icon">
                        <i class="fas fa-envelope"></i>
                        <input type="email" name="email" id="email" class="input" placeholder="Email Address (optional)" autocomplete="off" />
                    </div>
                </div>

                <div class="form-group">
                    <div class="input-icon">
                        <i class="fas fa-lock"></i>
                        <input type="password" name="password" id="password" class="input" placeholder="Password (min 6 characters)" autocomplete="off" required minlength="6" />
                        <i class="fas fa-eye toggle-password"></i>
                    </div>
                </div>

                <div class="form-group">
                    <div class="input-icon">
                        <i class="fas fa-lock"></i>
                        <input type="password" name="confirmPassword" id="confirmPassword" class="input" placeholder="Confirm Password" autocomplete="off" required minlength="6" />
                        <i class="fas fa-eye toggle-password"></i>
                    </div>
                </div>

                <div class="terms-group">
                    <input type="checkbox" id="terms" class="checkbox" required />
                    <label for="terms">I agree to the <a href="#">Terms of Service</a> and <a href="#">Privacy Policy</a></label>
                </div>

                <button type="submit" class="signup-button">
                    <span>Create Account</span>
                    <i class="fas fa-arrow-right"></i>
                </button>

                <% if (request.getAttribute("error") != null) { %>
                    <p id="signup-error" class="error-text"><%= request.getAttribute("error") %></p>
                <% } else { %>
                    <p id="signup-error" class="error-text"></p>
                <% } %>
            </form>

            <div class="social-signup">
                <p>Or sign up with</p>
                <div class="social-buttons">
                    <button class="social-button google" onclick="window.open('https://www.geeksforgeeks.org/simple-network-management-protocol-snmp/', '_blank')">
                        <i class="fab fa-google"></i>
                        Google
                    </button>
                    <button class="social-button github" onclick="window.open('https://github.com/noormostafa5/SNMP_GP', '_blank')">
                        <i class="fab fa-github"></i>
                        GitHub
                    </button>
                </div>
            </div>

            <p class="login-link">
                Already have an account? <a href="${pageContext.request.contextPath}/login">Login here</a>
            </p>
        </div>

        <div class="signup-image">
            <div class="image-content">
                <h2>Welcome to SNMP Platform</h2>
                <p>Join our community of network professionals and start monitoring your infrastructure today.</p>
                <div class="features-list">
                    <div class="feature-item">
                        <i class="fas fa-check-circle"></i>
                        <span>Real-time monitoring</span>
                    </div>
                    <div class="feature-item">
                        <i class="fas fa-check-circle"></i>
                        <span>Advanced analytics</span>
                    </div>
                    <div class="feature-item">
                        <i class="fas fa-check-circle"></i>
                        <span>24/7 support</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        // Toggle password visibility
        document.querySelectorAll('.toggle-password').forEach(toggle => {
            toggle.addEventListener('click', function() {
                const input = this.previousElementSibling;
                const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                input.setAttribute('type', type);
                this.classList.toggle('fa-eye');
                this.classList.toggle('fa-eye-slash');
            });
        });
    </script>
</body>
</html>