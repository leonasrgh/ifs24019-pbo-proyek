package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
// HAPUS import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor; // Deklarasi final

    // PERBAIKAN: Gunakan Constructor Injection
    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") 
                .excludePathPatterns(
                        "/auth/**",
                        "/assets/**",
                        "/api/auth/**",
                        "/error" 
                );
    }
}