package net.revirtualis.profat.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Cross-origin access for the REST API ({@code /api/v1/**}). Configure allowed origins via
 * {@link ProfatProperties#getCorsAllowedOriginPatterns()}; when empty, CORS is disabled.
 */
@Configuration
public class ProfatCorsConfiguration implements WebMvcConfigurer {

	private final ProfatProperties profatProperties;

	public ProfatCorsConfiguration(ProfatProperties profatProperties) {
		this.profatProperties = profatProperties;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		List<String> patterns = profatProperties.getCorsAllowedOriginPatterns();
		if (patterns.isEmpty()) {
			return;
		}
		registry.addMapping("/api/v1/**")
				.allowedOriginPatterns(patterns.toArray(String[]::new))
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
				.allowedHeaders("*")
				.maxAge(3600);
	}

	@Bean
	public FilterRegistrationBean<CorsFilter> profatCorsFilterRegistration() {
		FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>();
		List<String> patterns = profatProperties.getCorsAllowedOriginPatterns();
		if (patterns.isEmpty()) {
			registration.setEnabled(false);
			return registration;
		}
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(patterns);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
		config.setAllowedHeaders(List.of("*"));
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/v1/**", config);

		registration.setFilter(new CorsFilter(source));
		registration.addUrlPatterns("/*");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}
}
