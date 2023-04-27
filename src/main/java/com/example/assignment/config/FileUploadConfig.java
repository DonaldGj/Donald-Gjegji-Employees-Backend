package com.example.assignment.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;

public class FileUploadConfig {
    private final String uploadPath = "C:\\Users\\Donald Gjegji\\Documents\\assignment\\src\\main\\resources";

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(uploadPath);
        return factory.createMultipartConfig();
    }

}
