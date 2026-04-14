package com.hszg.DB_Management.Authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationFilterConfig {
	
	@Bean
    public FilterRegistrationBean<AuthenticationFilter> appAuthenticationFilter(@Value("${app.api.token}") String apiToken) {
        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthenticationFilter(apiToken));
        registrationBean.addUrlPatterns("/stations/*", "/delayReason/*", "/import/*", "/annotations/*", "/trips/*");
        return registrationBean;
    }
}
