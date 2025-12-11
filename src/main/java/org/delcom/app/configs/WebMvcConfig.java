package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // Terapkan ke SEMUA request (termasuk halaman HTML)
                .excludePathPatterns(
                        "/auth/**",          // Login & Register bebas akses
                        "/assets/**",        // CSS, JS, Gambar bebas akses
                        "/error",            // Halaman error
                        "/favicon.ico"       // Icon browser
                );
    }
}