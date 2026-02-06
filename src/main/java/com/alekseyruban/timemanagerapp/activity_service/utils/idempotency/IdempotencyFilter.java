package com.alekseyruban.timemanagerapp.activity_service.utils.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Object handlerObj =
                request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);

        if (!(handlerObj instanceof HandlerMethod handler)) {
            filterChain.doFilter(request, response);
            return;
        }

        Idempotent idempotent = handler.getMethodAnnotation(Idempotent.class);
        if (idempotent == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getHeader(IDEMPOTENCY_HEADER);
        if (key == null || key.isBlank()) {
            response.sendError(400, "Missing Idempotency-Key header");
            return;
        }

        String redisKey = buildRedisKey(request, key);

        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(response);

        byte[] requestBody = wrappedRequest.getInputStream().readAllBytes();
        wrappedRequest.getContentAsByteArray();

        String bodyHash = DigestUtils.sha256Hex(requestBody);

        String cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null && !cached.equals("IN_PROGRESS")) {
            CachedResponse cachedResponse =
                    objectMapper.readValue(cached, CachedResponse.class);

            if (!cachedResponse.getBodyHash().equals(bodyHash)) {
                response.sendError(409, "Idempotency key used with different body");
                return;
            }

            response.setStatus(cachedResponse.getStatus());
            cachedResponse.getHeaders()
                    .forEach(response::setHeader);
            response.getWriter().write(cachedResponse.getBody());
            return;
        }

        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(
                        redisKey,
                        "IN_PROGRESS",
                        Duration.ofSeconds(idempotent.lockSeconds())
                );

        if (Boolean.FALSE.equals(locked)) {
            response.sendError(409, "Request is already in progress");
            return;
        }

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception e) {
            redisTemplate.delete(redisKey);
            throw e;
        }

        String responseBody =
                new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

        CachedResponse cachedResponse = new CachedResponse(
                wrappedResponse.getStatus(),
                responseBody,
                extractHeaders(wrappedResponse),
                bodyHash
        );

        redisTemplate.opsForValue().set(
                redisKey,
                objectMapper.writeValueAsString(cachedResponse),
                Duration.ofSeconds(idempotent.ttlSeconds())
        );

        wrappedResponse.copyBodyToResponse();
    }

    private String buildRedisKey(HttpServletRequest request, String key) {
        return "idempotency:" + request.getMethod() + ":" + request.getRequestURI() + ":" + key;
    }

    private Map<String, String> extractHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        for (String name : response.getHeaderNames()) {
            headers.put(name, response.getHeader(name));
        }
        return headers;
    }
}