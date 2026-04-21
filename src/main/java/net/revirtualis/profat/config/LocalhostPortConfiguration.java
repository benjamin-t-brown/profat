package net.revirtualis.profat.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.tomcat.TomcatWebServerFactory;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LocalhostPortConfiguration {

	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> localhostPortConnectors(ProfatProperties profatProperties) {
		return factory -> {
			for (ProfatProperties.StaticSite site : profatProperties.getStaticSites()) {
				Integer p = site.getLocalhostPort();
				if (p != null && p > 0) {
					Connector connector = new Connector(TomcatWebServerFactory.DEFAULT_PROTOCOL);
					connector.setPort(p);
					connector.setProperty("address", "127.0.0.1");
					factory.addAdditionalConnectors(connector);
				}
			}
		};
	}

	@Bean
	public FilterRegistrationBean<LocalhostPortProxyFilter> localhostPortProxyFilterRegistration(ProfatProperties profatProperties) {
		Map<Integer, String> portToRoute = new HashMap<>();
		for (ProfatProperties.StaticSite site : profatProperties.getStaticSites()) {
			Integer p = site.getLocalhostPort();
			if (p != null && p > 0 && site.getRoute() != null && !site.getRoute().isBlank()) {
				portToRoute.put(p, site.getRoute().trim());
			}
		}
		LocalhostPortProxyFilter filter = new LocalhostPortProxyFilter(Map.copyOf(portToRoute));
		FilterRegistrationBean<LocalhostPortProxyFilter> bean = new FilterRegistrationBean<>(filter);
		bean.addUrlPatterns("/*");
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}
}
