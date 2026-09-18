package com.gsir.common.web;

import com.gsir.common.observability.ProjectMdcInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ProjectMdcInterceptor projectMdcInterceptor;

    public WebConfig(ProjectMdcInterceptor projectMdcInterceptor) {
        this.projectMdcInterceptor = projectMdcInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(projectMdcInterceptor);
    }
}
