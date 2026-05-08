package com.label.community.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DataEncryptionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        BufferedResponseWrapper wrappedResponse = new BufferedResponseWrapper(httpResponse);
        chain.doFilter(request, wrappedResponse);

        byte[] body = wrappedResponse.getCaptureAsBytes();
        String contentType = httpResponse.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            String raw = new String(body, StandardCharsets.UTF_8);
            String masked = raw
                .replaceAll("(\\\"phone\\\"\\s*:\\s*\\\"\\d{3})\\d{4}(\\d{4}\\\")", "$1****$2")
                .replaceAll("(\\\"idCardNo\\\"\\s*:\\s*\\\".{2}).+(.{2}\\\")", "$1************$2");
            byte[] bytes = masked.getBytes(StandardCharsets.UTF_8);
            httpResponse.setContentLength(bytes.length);
            httpResponse.getOutputStream().write(bytes);
            return;
        }

        httpResponse.setContentLength(body.length);
        httpResponse.getOutputStream().write(body);
    }
}
