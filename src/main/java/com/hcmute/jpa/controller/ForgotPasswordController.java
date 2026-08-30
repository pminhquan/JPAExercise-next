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
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/forgot-password")
public class ForgotPasswordController extends HttpServlet {

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

    public ForgotPasswordController(IUserService userService, IOtpService otpService, IEmailService emailService) {
        this.userService = userService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    public ForgotPasswordController() {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("cancel".equals(action)) {
            request.getSession().removeAttribute("pendingResetEmail");
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("request".equals(action)) {
            handleRequestOtp(request, response);
        } else if ("verify".equals(action)) {
            handleVerifyOtp(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
        }
    }

    private void handleRequestOtp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("error", "Email is required.");
            request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            request.setAttribute("error", "Please enter a valid email address.");
            request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
            return;
        }

        String successMsg = "If the email is registered, a password reset code has been sent.";

        User user = userService.findByEmail(email);
        if (user == null) {
            request.getSession().setAttribute("pendingResetEmail", email);
            request.setAttribute("message", successMsg);
            request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
            return;
        }

        try {
            String otp = otpService.generateOtp(user, OtpPurpose.FORGOT_PASSWORD);
            if (!emailService.sendOtpEmail(email, otp, OtpPurpose.FORGOT_PASSWORD)) {
                request.getSession().setAttribute("pendingResetEmail", email);
                request.setAttribute("message", successMsg);
                request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
                return;
            }

            request.setAttribute("message", successMsg);
            request.getSession().setAttribute("pendingResetEmail", email);
            request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);

        } catch (Exception e) {
            request.setAttribute("error", "Unable to process reset request. Please try again.");
            request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
        }
    }

    private void handleVerifyOtp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("pendingResetEmail");

        if (email == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String otp = request.getParameter("otp");
        if (otp == null || otp.trim().isEmpty()) {
            request.setAttribute("error", "Verification code is required.");
            request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
            return;
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            request.setAttribute("error", "Invalid or expired verification code.");
            request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
            return;
        }

        boolean verified = otpService.verifyOtp(user, OtpPurpose.FORGOT_PASSWORD, otp);

        if (verified) {
            session.removeAttribute("pendingResetEmail");

            session.setAttribute("resetEmail", email);
            session.setAttribute("resetAuthorized", true);
            session.setAttribute("resetExpiry", System.currentTimeMillis() + 5 * 60 * 1000);

            response.sendRedirect(request.getContextPath() + "/reset-password");
        } else {
            request.setAttribute("error", "Invalid or expired verification code.");
            request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
        }
    }
}
