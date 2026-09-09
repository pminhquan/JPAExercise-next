package com.hcmute.jpa.controller;

import com.hcmute.jpa.entity.User;
import com.hcmute.jpa.service.IUserService;
import com.hcmute.jpa.service.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/reset-password")
public class ResetPasswordController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private IUserService userService;

    @Override
    public void init() {
        this.userService = new UserServiceImpl();
    }

    public ResetPasswordController(IUserService userService) {
        this.userService = userService;
    }

    public ResetPasswordController() {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!validateResetAuth(request)) {
            invalidateAuthSession(request);
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!validateResetAuth(request)) {
            invalidateAuthSession(request);
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (password == null || password.trim().isEmpty() || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            request.setAttribute("error", "Passwords cannot be blank.");
            request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
            return;
        }

        if (password.length() < 6) {
            request.setAttribute("error", "Password must be at least 6 characters.");
            request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("resetEmail");
        User user = userService.findByEmail(email);

        if (user == null) {
            request.setAttribute("error", "Invalid user mapping.");
            request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
            return;
        }

        try {
            boolean updated = userService.updatePassword(user.getId(), password);
            if (updated) {
                invalidateAuthSession(request);
                response.sendRedirect(request.getContextPath() + "/login?message=Password reset successful. Please login.");
            } else {
                request.setAttribute("error", "Failed to update password.");
                request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", "Unable to reset password. Please try again.");
            request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
        }
    }

    private boolean validateResetAuth(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String email = (String) session.getAttribute("resetEmail");
        Boolean authorized = (Boolean) session.getAttribute("resetAuthorized");
        Long expiry = (Long) session.getAttribute("resetExpiry");

        if (email == null || authorized == null || !authorized || expiry == null) {
            return false;
        }

        if (System.currentTimeMillis() > expiry) {
            return false;
        }

        return true;
    }

    private void invalidateAuthSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("resetEmail");
            session.removeAttribute("resetAuthorized");
            session.removeAttribute("resetExpiry");
        }
    }
}
