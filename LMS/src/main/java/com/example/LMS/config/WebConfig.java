package com.example.LMS.config;
import com.example.LMS.interceptor.LoggingInterceptor;
import com.example.LMS.interceptor.ValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final ValidationInterceptor validationInterceptor;

    @Autowired
    public WebConfig(LoggingInterceptor loggingInterceptor, ValidationInterceptor validationInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.validationInterceptor = validationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(validationInterceptor)
                .addPathPatterns("/books/**", "/students/**", "/borrowings/**");
    }
}