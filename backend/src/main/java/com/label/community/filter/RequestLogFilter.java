package com.label.community.filter;

import com.label.community.dao.OperationLogDao;
import com.label.community.security.AuthContext;
import com.label.community.security.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RequestLogFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);
    private final OperationLogDao operationLogDao = new OperationLogDao();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long start = System.currentTimeMillis();
        Exception thrown = null;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException throwable) {
            thrown = throwable;
            throw throwable;
        } finally {
            long cost = System.currentTimeMillis() - start;
            AuthUser authUser = AuthContext.get();
            String path = httpRequest.getRequestURI();
            String method = httpRequest.getMethod();
            int status = httpResponse.getStatus();
            if (thrown != null && status < 400) {
                status = 500;
            }

            log.info("request method={} path={} status={} cost={}ms user={}", method, path, status, cost,
                authUser == null ? "anonymous" : authUser.getUsername());

            operationLogDao.insert(
                authUser == null ? null : authUser.getUserId(),
                authUser == null ? null : authUser.getUsername(),
                method + " " + path,
                path,
                method,
                status,
                cost
            );
        }
    }
}
