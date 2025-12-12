package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    // 1. TAMBAHKAN INI: Agar gambar yang diupload bisa muncul di browser
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get("./uploads").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

    // 2. PERBAIKAN INTERCEPTOR: Agar tidak looping redirect
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") 
                .excludePathPatterns(
                        "/login",       // <--- WAJIB ADA (Sesuai Controller)
                        "/register",    // <--- WAJIB ADA (Sesuai Controller)
                        "/auth/**",     // Jaga-jaga
                        "/uploads/**",  // Agar gambar bisa dilihat tanpa login (opsional)
                        "/assets/**",   
                        "/css/**",      
                        "/js/**",
                        "/images/**",
                        "/error",       
                        "/favicon.ico"
                );
    }
}