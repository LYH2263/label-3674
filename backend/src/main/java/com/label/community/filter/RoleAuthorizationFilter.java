package com.label.community.filter;

import com.label.community.common.ApiResponse;
import com.label.community.common.JsonUtil;
import com.label.community.config.RoleConstants;
import com.label.community.security.AuthContext;
import com.label.community.security.AuthUser;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class RoleAuthorizationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        AuthUser authUser = AuthContext.get();

        if (authUser != null) {
            if (path.startsWith("/api/repairs/") && path.endsWith("/status") && "PATCH".equalsIgnoreCase(method)) {
                if (!Set.of(RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER).contains(authUser.getRole())) {
                    JsonUtil.writeResponse(httpResponse, 403, ApiResponse.error(40320, "无权执行该操作"));
                    return;
                }
            }
            if (path.startsWith("/api/property") && !RoleConstants.RESIDENT.equals(authUser.getRole())) {
                JsonUtil.writeResponse(httpResponse, 403, ApiResponse.error(40321, "仅居民可访问物业服务模块"));
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
