package com.label.community.filter;

import com.label.community.common.ApiResponse;
import com.label.community.common.BusinessException;
import com.label.community.common.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ExceptionHandlingFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {
            chain.doFilter(request, response);
        } catch (BusinessException ex) {
            JsonUtil.writeResponse(httpResponse, ex.getStatus(), ApiResponse.error(ex.getCode(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unhandled exception", ex);
            JsonUtil.writeResponse(httpResponse, 500, ApiResponse.error(50000, "系统繁忙，请稍后重试"));
        }
    }
}
