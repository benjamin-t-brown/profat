package net.revirtualis.profat.service;

import net.revirtualis.profat.config.ProfatProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
public class StaticSiteRegistry {

	private final Map<String, ProfatProperties.StaticSite> byRoute;

	public StaticSiteRegistry(ProfatProperties profatProperties) {
		Map<String, ProfatProperties.StaticSite> map = new HashMap<>();
		for (ProfatProperties.StaticSite site : profatProperties.getStaticSites()) {
			if (site.getRoute() == null || site.getRoute().isBlank()) {
				continue;
			}
			String key = normalizeRoute(site.getRoute());
			map.put(key, site);
		}
		this.byRoute = Collections.unmodifiableMap(map);
	}

	public Optional<ProfatProperties.StaticSite> findByRoute(String route) {
		if (route == null || route.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(byRoute.get(normalizeRoute(route)));
	}

	private static String normalizeRoute(String route) {
		return route.trim().toLowerCase(Locale.ROOT);
	}
}
