package com.breeding.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Collections.singletonList("*")); // 允许所有域名
        config.setAllowedMethods(Collections.singletonList("*"));        // 允许所有请求方法
        config.setAllowedHeaders(Collections.singletonList("*"));        // 允许所有请求头
        config.setAllowCredentials(false);                               // 不允许携带凭证(Cookie)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);                 // 对所有路径生效

        return new CorsFilter(source);
    }
}
