package com.hcmute.jpa.controller;

import com.hcmute.jpa.config.JpaConfig;
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
    private TransactionBoundary transactionBoundary;

    @Override
    public void init() {
        this.userService = new UserServiceImpl();
        this.otpService = new OtpServiceImpl();
        this.transactionBoundary = new JpaTransactionBoundary();
    }

    public VerifyOtpController(IUserService userService, IOtpService otpService) {
        this(userService, otpService, new JpaTransactionBoundary());
    }

    public VerifyOtpController(IUserService userService, IOtpService otpService,
                               TransactionBoundary transactionBoundary) {
        this.userService = userService;
        this.otpService = otpService;
        this.transactionBoundary = transactionBoundary;
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

        transactionBoundary.begin();
        try {
            boolean verified = otpService.verifyOtp(user, OtpPurpose.REGISTER, otp);

            if (verified) {
                boolean activated = userService.activateUser(user.getId());
                if (activated) {
                    transactionBoundary.commit();
                    request.getSession().removeAttribute("pendingVerifyEmail");
                    request.setAttribute("success", "Your account has been successfully verified and activated!");
                } else {
                    transactionBoundary.rollback();
                    request.setAttribute("error", "Failed to activate user account.");
                }
            } else {
                transactionBoundary.commit();
                request.setAttribute("error", "Invalid, expired, or blocked verification code.");
            }
        } catch (Exception e) {
            transactionBoundary.rollback();
            throw e;
        } finally {
            transactionBoundary.end();
        }

        request.getRequestDispatcher("/views/verify-otp.jsp").forward(request, response);
    }

    public interface TransactionBoundary {

        void begin();

        void commit();

        void rollback();

        void end();
    }

    private static final class JpaTransactionBoundary implements TransactionBoundary {

        @Override
        public void begin() {
            JpaConfig.beginTransaction();
        }

        @Override
        public void commit() {
            JpaConfig.commitTransaction();
        }

        @Override
        public void rollback() {
            JpaConfig.rollbackTransaction();
        }

        @Override
        public void end() {
            JpaConfig.endTransaction();
        }
    }

}
