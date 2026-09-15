package com.abhinay.proxy_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReverseProxyController {

    private final StringRedisTemplate redisTemplate;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Hop-by-hop headers to exclude when proxying
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailers", "transfer-encoding", "upgrade", "host", "content-length"
    );

    @RequestMapping("/**")
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String rawHost = request.getHeader("Host");
        if (rawHost == null || rawHost.isBlank()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing Host header");
            return;
        }

        String hostname = rawHost.split(":")[0];
        String targetIp = redisTemplate.opsForValue().get("route:" + hostname);

        if (targetIp == null || targetIp.isBlank()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType("text/plain");
            response.getWriter().write("Preview not found for " + hostname + ".");
            return;
        }

        String targetBaseUrl = targetIp.startsWith("http://") || targetIp.startsWith("https://")
                ? targetIp
                : (targetIp.contains(":") ? "http://" + targetIp : "http://" + targetIp + ":5173");

        String queryString = request.getQueryString();
        String fullUrl = targetBaseUrl + request.getRequestURI() + (queryString != null ? "?" + queryString : "");

        log.info("HTTP Proxy: {} -> {}", hostname, fullUrl);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(fullUrl));

        // Copy incoming headers
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase())) {
                    Enumeration<String> headerValues = request.getHeaders(headerName);
                    while (headerValues.hasMoreElements()) {
                        requestBuilder.header(headerName, headerValues.nextElement());
                    }
                }
            }
        }

        // Set request method and body
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            requestBuilder.GET();
        } else if ("DELETE".equalsIgnoreCase(method)) {
            requestBuilder.DELETE();
        } else {
            InputStream bodyStream = request.getInputStream();
            requestBuilder.method(method, HttpRequest.BodyPublishers.ofInputStream(() -> bodyStream));
        }

        try {
            HttpResponse<InputStream> proxyResponse = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            response.setStatus(proxyResponse.statusCode());

            // Copy response headers
            proxyResponse.headers().map().forEach((headerName, headerValues) -> {
                if (!HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase())) {
                    for (String headerValue : headerValues) {
                        response.addHeader(headerName, headerValue);
                    }
                }
            });

            // Stream response body back to client
            try (InputStream in = proxyResponse.body(); OutputStream out = response.getOutputStream()) {
                in.transferTo(out);
                out.flush();
            }
        } catch (Exception e) {
            log.error("Proxy Error for {}: {}", hostname, e.getMessage());
            if (!response.isCommitted()) {
                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                response.setHeader("Retry-After", "3");
                response.setContentType("text/html;charset=UTF-8");
                String html = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta http-equiv="refresh" content="3">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Starting Dev Server...</title>
                        <style>
                            body {
                                margin: 0;
                                padding: 0;
                                background-color: #0f172a;
                                color: #f8fafc;
                                font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                                justify-content: center;
                                height: 100vh;
                                overflow: hidden;
                            }
                            .card {
                                background-color: #1e293b;
                                border: 1px solid #334155;
                                border-radius: 12px;
                                padding: 32px 40px;
                                text-align: center;
                                max-width: 400px;
                                box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5);
                            }
                            .spinner {
                                width: 44px;
                                height: 44px;
                                border: 4px solid #334155;
                                border-top: 4px solid #6366f1;
                                border-radius: 50%;
                                animation: spin 1s linear infinite;
                                margin: 0 auto 20px auto;
                            }
                            @keyframes spin {
                                0% { transform: rotate(0deg); }
                                100% { transform: rotate(360deg); }
                            }
                            h2 {
                                font-size: 18px;
                                font-weight: 600;
                                margin: 0 0 8px 0;
                                color: #f1f5f9;
                            }
                            p {
                                font-size: 13px;
                                color: #94a3b8;
                                margin: 0;
                                line-height: 1.5;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="card">
                            <div class="spinner"></div>
                            <h2>Setting up Preview Environment</h2>
                            <p>Syncing project files and starting Vite server... Page will auto-reload in a few seconds.</p>
                        </div>
                    </body>
                    </html>
                    """;
                response.getWriter().write(html);
            }
        }
    }
}
