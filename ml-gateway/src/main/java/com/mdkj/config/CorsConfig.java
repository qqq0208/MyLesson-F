package com.mdkj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 1. 明确写上所有前端域名，不要用 *
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:24108",
                "http://127.0.0.1:24108"
        ));
        // 允许全部请求方式
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 放行所有请求头
        config.addAllowedHeader("*");
        // 允许携带凭证（token、cookie）
        config.setAllowCredentials(true);
        // 预检有效期 1小时
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}