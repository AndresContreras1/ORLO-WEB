package com.orlo.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Tope al tamaño del cuerpo de cualquier petición (por defecto 1 MB). Tomcat solo limita los
 * formularios, no el JSON: sin esto un cuerpo de cientos de MB llega entero a memoria.
 * Con Content-Length responde 413 sin leer nada; sin él (chunked) corta la lectura al pasar el tope.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestSizeLimitFilter extends OncePerRequestFilter {

    private final long maxBytes;
    private final ProblemDetailWriter problemDetailWriter;

    public RequestSizeLimitFilter(@Value("${app.http.max-body-bytes:1048576}") long maxBytes,
                                  ProblemDetailWriter problemDetailWriter) {
        this.maxBytes = maxBytes;
        this.problemDetailWriter = problemDetailWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long length = request.getContentLengthLong();
        if (length > maxBytes) {
            problemDetailWriter.write(response, HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE",
                    "La petición es demasiado grande");
            return;
        }
        chain.doFilter(length < 0 ? new LimitedRequest(request, maxBytes) : request, response);
    }

    /** Petición sin Content-Length: el cuerpo se lee hasta el tope y después falla. */
    private static final class LimitedRequest extends HttpServletRequestWrapper {

        private final long maxBytes;

        LimitedRequest(HttpServletRequest request, long maxBytes) {
            super(request);
            this.maxBytes = maxBytes;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            ServletInputStream original = super.getInputStream();
            return new ServletInputStream() {
                private long read;

                @Override
                public int read() throws IOException {
                    int b = original.read();
                    if (b != -1 && ++read > maxBytes) {
                        throw new IOException("Cuerpo más grande que el máximo permitido");
                    }
                    return b;
                }

                @Override
                public int read(byte[] buffer, int off, int len) throws IOException {
                    int n = original.read(buffer, off, len);
                    if (n > 0 && (read += n) > maxBytes) {
                        throw new IOException("Cuerpo más grande que el máximo permitido");
                    }
                    return n;
                }

                @Override
                public boolean isFinished() {
                    return original.isFinished();
                }

                @Override
                public boolean isReady() {
                    return original.isReady();
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    original.setReadListener(listener);
                }
            };
        }
    }
}
