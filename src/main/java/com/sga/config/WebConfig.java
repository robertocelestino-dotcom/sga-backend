package com.sga.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 🔥 Timeout para chamadas assíncronas: 20 minutos
        configurer.setDefaultTimeout(1200000);
        // 🔥 Task executor com pool maior
        configurer.setTaskExecutor(new org.springframework.core.task.SimpleAsyncTaskExecutor());
    }
}
