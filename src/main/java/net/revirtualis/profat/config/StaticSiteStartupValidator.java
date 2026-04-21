package net.revirtualis.profat.config;

import net.revirtualis.profat.repository.ServiceRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Validates static site configuration once services and the DB are available.
 */
@Component
@Order(100)
public class StaticSiteStartupValidator implements ApplicationRunner {

	private final ProfatProperties profatProperties;
	private final ServiceRepository serviceRepository;
	private final Environment environment;

	public StaticSiteStartupValidator(
			ProfatProperties profatProperties,
			ServiceRepository serviceRepository,
			Environment environment) {
		this.profatProperties = profatProperties;
		this.serviceRepository = serviceRepository;
		this.environment = environment;
	}

	@Override
	public void run(ApplicationArguments args) {
		int serverPort = environment.getProperty("server.port", Integer.class, 8080);
		Set<String> seenRoutes = new HashSet<>();
		Set<Integer> seenLocalhostPorts = new HashSet<>();
		for (ProfatProperties.StaticSite site : profatProperties.getStaticSites()) {
			if (site.getRoute() == null || site.getRoute().isBlank()) {
				throw new IllegalStateException("profat.static-sites entry has a blank route");
			}
			if (site.getStaticFiles() == null) {
				throw new IllegalStateException("profat.static-sites route '" + site.getRoute() + "' has no static-files path");
			}
			if (site.getAnalyticsName() == null || site.getAnalyticsName().isBlank()) {
				throw new IllegalStateException("profat.static-sites route '" + site.getRoute() + "' has a blank analytics-name");
			}
			if (site.getServiceId() == null) {
				throw new IllegalStateException("profat.static-sites route '" + site.getRoute() + "' has no service-id");
			}
			String normalized = site.getRoute().trim().toLowerCase(Locale.ROOT);
			if (!seenRoutes.add(normalized)) {
				throw new IllegalStateException("Duplicate profat.static-sites route: " + site.getRoute());
			}
			if (normalized.contains("/") || normalized.contains("..")) {
				throw new IllegalStateException("profat.static-sites route must be a single path segment: " + site.getRoute());
			}
			Path root = site.getStaticFiles().toAbsolutePath().normalize();
			if (!Files.isDirectory(root)) {
				throw new IllegalStateException("profat.static-sites static-files is not a directory: " + root);
			}
			if (!serviceRepository.existsById(site.getServiceId())) {
				throw new IllegalStateException(
						"No registered service with id " + site.getServiceId() + " for static route '" + site.getRoute() + "'");
			}
			Integer lp = site.getLocalhostPort();
			if (lp != null) {
				if (lp <= 0 || lp > 65535) {
					throw new IllegalStateException(
							"profat.staticSites localhost-port for route '" + site.getRoute() + "' must be between 1 and 65535");
				}
				if (lp == serverPort) {
					throw new IllegalStateException(
							"profat.staticSites localhost-port " + lp + " for route '" + site.getRoute()
									+ "' must not equal server.port (" + serverPort + ")");
				}
				if (!seenLocalhostPorts.add(lp)) {
					throw new IllegalStateException("Duplicate profat.staticSites localhost-port: " + lp);
				}
			}
		}
	}
}
