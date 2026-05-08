package com.label.community.common;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

public final class RequestUtil {
    private RequestUtil() {
    }

    public static String readBody(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException ex) {
            throw new BusinessException(400, 40002, "读取请求体失败");
        }
        return builder.toString();
    }
}
