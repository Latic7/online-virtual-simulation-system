package org.ovss.onlinevirtualsimulationsystem.config;

import org.ovss.onlinevirtualsimulationsystem.interceptor.AdminRedirectInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminRedirectInterceptor adminRedirectInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminRedirectInterceptor).addPathPatterns("/home", "/profile");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
        String uploadBase = uploadRoot.toFile().getAbsolutePath().replace("\\", "/");
        String modelsFs = (uploadBase.startsWith("/") ? "file://" : "file:///") + uploadBase + "/models/";
        String thumbsFs = (uploadBase.startsWith("/") ? "file://" : "file:///") + uploadBase + "/thumbnails/";

        // Serve models and thumbnails exclusively from external uploads directory
        registry.addResourceHandler("/models/**")
                .addResourceLocations(modelsFs);

        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations(thumbsFs);
    }
}
