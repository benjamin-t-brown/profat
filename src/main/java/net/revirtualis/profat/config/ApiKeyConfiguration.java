package net.revirtualis.profat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class ApiKeyConfiguration {

	@Bean
	public FilterRegistrationBean<ApiKeyAuthenticationFilter> apiKeyAuthenticationFilterRegistration(
			ProfatProperties profatProperties,
			ObjectMapper objectMapper) {
		ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(profatProperties, objectMapper);
		FilterRegistrationBean<ApiKeyAuthenticationFilter> bean = new FilterRegistrationBean<>(filter);
		bean.addUrlPatterns("/*");
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
		return bean;
	}
}
