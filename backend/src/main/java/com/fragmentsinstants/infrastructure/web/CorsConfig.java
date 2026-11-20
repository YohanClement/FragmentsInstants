package com.fragmentsinstants.infrastructure.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer{
    @Value ("${fragments-instants.cors.origine-frontend}")
    private String origineFrontend;

    @Override
    public void addCorsMappings(CorsRegistry registry){
       registry.addMapping("/api/**").allowedOrigins(origineFrontend).allowedMethods("GET","PUT","POST","DELETE"); 
    }

}
