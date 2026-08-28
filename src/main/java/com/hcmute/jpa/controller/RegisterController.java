package com.hcmute.jpa.controller;

import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.entity.User;
import com.hcmute.jpa.service.EmailServiceImpl;
import com.hcmute.jpa.service.IEmailService;
import com.hcmute.jpa.service.IOtpService;
import com.hcmute.jpa.service.IUserService;
import com.hcmute.jpa.service.OtpServiceImpl;
import com.hcmute.jpa.service.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/register")
public class RegisterController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private IUserService userService;
    private IOtpService otpService;
    private IEmailService emailService;

    @Override
    public void init() {
        this.userService = new UserServiceImpl();
        this.otpService = new OtpServiceImpl();
        this.emailService = new EmailServiceImpl();
    }

    public RegisterController(IUserService userService, IOtpService otpService, IEmailService emailService) {
        this.userService = userService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    public RegisterController() {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/register.jsp").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (isEmpty(username) || isEmpty(email) || isEmpty(password) || isEmpty(confirmPassword)) {
            request.setAttribute("error", "All fields are required.");
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            request.setAttribute("error", "Please enter a valid email address.");
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
            return;
        }

        User user = null;
        User existingUserByUsername = userService.findByUsername(username);
        User existingUserByEmail = userService.findByEmail(email);

        if (existingUserByUsername != null) {
            if (existingUserByUsername.isActive()) {
                request.setAttribute("error", "Username is already registered.");
                request.getRequestDispatcher("/views/register.jsp").forward(request, response);
                return;
            } else {
                if (!existingUserByUsername.getEmail().equalsIgnoreCase(email)) {
                    request.setAttribute("error", "Username is already registered.");
                    request.getRequestDispatcher("/views/register.jsp").forward(request, response);
                    return;
                }
                user = existingUserByUsername;
            }
        }

        if (existingUserByEmail != null) {
            if (existingUserByEmail.isActive()) {
                request.setAttribute("error", "Email is already registered.");
                request.getRequestDispatcher("/views/register.jsp").forward(request, response);
                return;
            } else {
                if (!existingUserByEmail.getUsername().equalsIgnoreCase(username)) {
                    request.setAttribute("error", "Email is already registered.");
                    request.getRequestDispatcher("/views/register.jsp").forward(request, response);
                    return;
                }
                user = existingUserByEmail;
            }
        }

        try {
            if (user == null) {
                user = userService.register(username, email, password);
            }

            String otp = otpService.generateOtp(user, OtpPurpose.REGISTER);

            boolean emailSent = emailService.sendOtpEmail(email, otp, OtpPurpose.REGISTER);

            if (!emailSent) {
                request.setAttribute("error", "Failed to send verification email. Please try again.");
                request.getRequestDispatcher("/views/register.jsp").forward(request, response);
                return;
            }

            request.getSession().setAttribute("pendingVerifyEmail", email);
            response.sendRedirect(request.getContextPath() + "/verify-otp");

        } catch (Exception e) {
            request.setAttribute("error", "Registration could not be completed. Please try again.");
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
