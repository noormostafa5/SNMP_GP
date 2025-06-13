<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>SNMP Platform - Welcome</title>
    <link rel="stylesheet" type="text/css" href="../Styles/welcome.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        body {
            background-image: url('../background/bckg1.jpg');
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
    <div class="welcome-container">
        <nav class="top-nav">
            <div class="logo">
                <i class="fas fa-network-wired"></i>
                <span>SNMP Platform</span>
            </div>
            <div class="nav-buttons">
                <a href="login.jsp" class="nav-button">Login</a>
                <a href="signUp.jsp" class="nav-button primary">Sign Up</a>
            </div>
        </nav>

        <div class="hero-section">
            <div class="hero-content">
                <h1 class="hero-title">Network Monitoring Made Simple</h1>
                <p class="hero-subtitle">Monitor, manage, and optimize your network infrastructure with our powerful SNMP platform</p>
                <div class="hero-buttons">
                    <a href="signUp.jsp" class="hero-button primary">Get Started</a>
                    <a href="#features" class="hero-button secondary">Learn More</a>
                </div>
            </div>
            <div class="hero-image">
                <div class="network-animation">
                    <i class="fas fa-server"></i>
                    <i class="fas fa-network-wired"></i>
                    <i class="fas fa-shield-alt"></i>
                </div>
            </div>
        </div>

        <div id="features" class="features-section">
            <h2 class="section-title">Why Choose Our Platform?</h2>
            <div class="features-grid">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-chart-line"></i>
                    </div>
                    <h3>Real-time Monitoring</h3>
                    <p>Track your network performance in real-time with detailed metrics and analytics</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-bell"></i>
                    </div>
                    <h3>Smart Alerts</h3>
                    <p>Get instant notifications about network issues and performance anomalies</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-cogs"></i>
                    </div>
                    <h3>Easy Configuration</h3>
                    <p>Simple setup and configuration of network devices and monitoring rules</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-shield-alt"></i>
                    </div>
                    <h3>Security First</h3>
                    <p>Enterprise-grade security to protect your network infrastructure</p>
                </div>
            </div>
        </div>

        <div class="cta-section">
            <div class="cta-content">
                <h2>Ready to Get Started?</h2>
                <p>Join thousands of network administrators who trust our platform</p>
                <a href="signUp.jsp" class="cta-button">Start Free Trial</a>
            </div>
        </div>

        <footer class="footer">
            <div class="footer-content">
                <div class="footer-section">
                    <h4>SNMP Platform</h4>
                    <p>Your trusted partner in network monitoring and management</p>
                </div>
                <div class="footer-section">
                    <h4>Quick Links</h4>
                    <a href="login.jsp">Login</a>
                    <a href="signUp.jsp">Sign Up</a>
                    <a href="#features">Features</a>
                </div>
                <div class="footer-section">
                    <h4>Contact</h4>
                    <p><i class="fas fa-envelope"></i> support@snmp-platform.com</p>
                    <p><i class="fas fa-phone"></i> +1 (555) 123-4567</p>
                </div>
            </div>
            <div class="footer-bottom">
                <p>&copy; 2025 SNMP Platform. All rights reserved.</p>
            </div>
        </footer>
    </div>
</body>
</html>