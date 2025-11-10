package org.ovss.onlinevirtualsimulationsystem.config;

import org.ovss.onlinevirtualsimulationsystem.interceptor.AdminRedirectInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminRedirectInterceptor adminRedirectInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminRedirectInterceptor).addPathPatterns("/home", "/profile");
    }
}

