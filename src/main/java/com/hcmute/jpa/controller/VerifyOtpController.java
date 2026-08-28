package com.hcmute.jpa.controller;

import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.entity.User;
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

@WebServlet("/verify-otp")
public class VerifyOtpController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private IUserService userService;
    private IOtpService otpService;

    @Override
    public void init() {
        this.userService = new UserServiceImpl();
        this.otpService = new OtpServiceImpl();
    }

    public VerifyOtpController(IUserService userService, IOtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    public VerifyOtpController() {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = (String) request.getSession().getAttribute("pendingVerifyEmail");
        if (email == null) {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }

        request.getRequestDispatcher("/views/verify-otp.jsp").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = (String) request.getSession().getAttribute("pendingVerifyEmail");
        if (email == null) {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }

        String otp = request.getParameter("otp");
        if (otp == null || otp.trim().isEmpty()) {
            request.setAttribute("error", "Verification code is required.");
            request.getRequestDispatcher("/views/verify-otp.jsp").forward(request, response);
            return;
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            request.setAttribute("error", "Invalid user session.");
            request.getRequestDispatcher("/views/verify-otp.jsp").forward(request, response);
            return;
        }

        boolean verified = otpService.verifyOtp(user, OtpPurpose.REGISTER, otp);

        if (verified) {
            boolean activated = userService.activateUser(user.getId());
            if (activated) {
                request.getSession().removeAttribute("pendingVerifyEmail");
                request.setAttribute("success", "Your account has been successfully verified and activated!");
            } else {
                request.setAttribute("error", "Failed to activate user account.");
            }
        } else {
            request.setAttribute("error", "Invalid, expired, or blocked verification code.");
        }

        request.getRequestDispatcher("/views/verify-otp.jsp").forward(request, response);
    }
}
