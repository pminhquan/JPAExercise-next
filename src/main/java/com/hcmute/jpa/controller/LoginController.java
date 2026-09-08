package com.hcmute.jpa.controller;

import com.hcmute.jpa.entity.Role;
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

@WebServlet("/login")
public class LoginController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private IUserService userService;

    @Override
    public void init() {
        this.userService = new UserServiceImpl();
    }

    public LoginController(IUserService userService) {
        this.userService = userService;
    }

    public LoginController() {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/login.jsp").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String identifier = request.getParameter("identifier");
        String password = request.getParameter("password");

        String genericError = "Invalid username/email or password.";

        if (isEmpty(identifier) || isEmpty(password)) {
            request.setAttribute("error", genericError);
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        User user = userService.findByEmail(identifier);
        if (user == null) {
            user = userService.findByUsername(identifier);
        }

        if (user == null) {
            request.setAttribute("error", genericError);
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        if (!user.isActive()) {
            request.setAttribute("error", genericError);
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        boolean verified = userService.verifyPassword(user, password);

        if (verified) {
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("authenticatedUserId", user.getId());
            Role role = user.getRole() != null ? user.getRole() : Role.CUSTOMER;
            session.setAttribute("authenticatedUserRole", role.name());
            session.setAttribute("account", user);

            response.sendRedirect(request.getContextPath() + "/categories");
        } else {
            request.setAttribute("error", genericError);
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
