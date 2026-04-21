package net.revirtualis.profat.service;

import net.revirtualis.profat.config.ProfatProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Actions that carry page-visit style payloads and participate in analytics summaries
 * ({@code page_visit} plus each static site's {@code analyticsName}).
 */
@Component
public class PageVisitActionCatalog {

	private static final String DEFAULT_PAGE_VISIT = "page_visit";

	private final List<String> actions;
	private final Set<String> actionSet;

	public PageVisitActionCatalog(ProfatProperties profatProperties) {
		LinkedHashSet<String> set = new LinkedHashSet<>();
		set.add(DEFAULT_PAGE_VISIT);
		for (ProfatProperties.StaticSite site : profatProperties.getStaticSites()) {
			if (site.getAnalyticsName() != null && !site.getAnalyticsName().isBlank()) {
				set.add(site.getAnalyticsName().trim());
			}
		}
		this.actionSet = Collections.unmodifiableSet(set);
		this.actions = Collections.unmodifiableList(new ArrayList<>(set));
	}

	public List<String> listActions() {
		return actions;
	}

	public Set<String> asSet() {
		return actionSet;
	}

	public boolean isPageVisitLike(String action) {
		return action != null && actionSet.contains(action.trim());
	}
}
