package com.label.community.filter;

import com.label.community.common.ApiResponse;
import com.label.community.common.JsonUtil;
import com.label.community.security.AuthContext;
import com.label.community.security.AuthUser;
import com.label.community.security.JwtUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class JwtAuthenticationFilter implements Filter {
    private static final Set<String> OPEN_PATHS = Set.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/health",
        "/api/openapi.json"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        boolean open = OPEN_PATHS.contains(path) || path.startsWith("/api/files/");
        String authorization = httpRequest.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            AuthUser authUser = JwtUtil.parse(token);
            AuthContext.set(authUser);
            httpRequest.setAttribute("authUser", authUser);
        }

        try {
            if (!open && AuthContext.get() == null) {
                JsonUtil.writeResponse(httpResponse, 401, ApiResponse.error(40100, "未登录或登录已过期"));
                return;
            }
            chain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }
}
