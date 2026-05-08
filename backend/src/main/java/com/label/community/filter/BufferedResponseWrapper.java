package com.label.community.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class BufferedResponseWrapper extends HttpServletResponseWrapper {
    private final ByteArrayOutputStream capture = new ByteArrayOutputStream();
    private ServletOutputStream output;
    private PrintWriter writer;

    public BufferedResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        if (writer != null) {
            throw new IllegalStateException("Writer already obtained");
        }
        if (output == null) {
            output = new ServletOutputStream() {
                @Override
                public void write(int b) {
                    capture.write(b);
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                }
            };
        }
        return output;
    }

    @Override
    public PrintWriter getWriter() {
        if (output != null) {
            throw new IllegalStateException("OutputStream already obtained");
        }
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(capture, StandardCharsets.UTF_8));
        }
        return writer;
    }

    public byte[] getCaptureAsBytes() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        if (output != null) {
            output.flush();
        }
        return capture.toByteArray();
    }
}
