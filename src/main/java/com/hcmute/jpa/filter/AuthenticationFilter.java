package com.hcmute.jpa.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {
        "/profile", "/profile/*",
        "/categories", "/categories/*",
        "/products", "/products/*",
        "/product", "/product/*",
        "/admin/product", "/admin/product/*",
        "/admin/category", "/admin/category/*"
})
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getServletPath();

        if ("/product".equals(path)
                || "/products/detail".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("authenticatedUserId") != null);

        if (!isLoggedIn) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        if (isAdminRoute(path)) {
            boolean isAdmin = "ADMIN".equals(session.getAttribute("authenticatedUserRole"));
            if (!isAdmin) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        if (path != null) {
            if (matchesRoute(path, "/admin/product")) {
                String sub = path.substring("/admin/product".length());
                String target = sub.isEmpty() || sub.equals("/") ? "/products" : "/products" + (sub.startsWith("/") ? sub : "/" + sub);
                httpRequest.getRequestDispatcher(target).forward(request, response);
                return;
            }
            if (matchesRoute(path, "/admin/category")) {
                String sub = path.substring("/admin/category".length());
                String target = sub.isEmpty() || sub.equals("/") ? "/categories" : "/categories" + (sub.startsWith("/") ? sub : "/" + sub);
                httpRequest.getRequestDispatcher(target).forward(request, response);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isAdminRoute(String path) {
        if (path == null || "/products/detail".equals(path)) {
            return false;
        }
        return matchesRoute(path, "/products")
                || matchesRoute(path, "/categories")
                || matchesRoute(path, "/admin/product")
                || matchesRoute(path, "/admin/category");
    }

    private boolean matchesRoute(String path, String base) {
        if (path == null) {
            return false;
        }
        return path.equals(base) || path.startsWith(base + "/");
    }

    @Override
    public void destroy() {
    }
}
