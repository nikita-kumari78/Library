package com.example.LMS.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ValidationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ValidationInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug("Validation Interceptor: Pre-handling request for [{}]", request.getRequestURI());

        return true;
    }
}