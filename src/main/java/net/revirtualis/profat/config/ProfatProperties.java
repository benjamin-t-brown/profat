package net.revirtualis.profat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ConfigurationProperties(prefix = "profat")
public class ProfatProperties {

	/**
	 * Shared secret for REST API access (header {@code X-Profat-Key}). If blank, API key checks are disabled
	 * (unless {@link #isApiKeyRequired()}).
	 */
	private String apiKey = "";

	/**
	 * When true, the application refuses to start if {@link #getApiKey()} is blank. Enable for production
	 * ({@code prod} profile); for local dev, set the key in gitignored {@code application-local.properties} or env.
	 */
	private boolean apiKeyRequired = false;

	/**
	 * Static sites served under {@code /proxy/{route}/...} (mount paths, asset URLs, etc. can be handled in nginx).
	 */
	private List<StaticSite> staticSites = new ArrayList<>();

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey != null ? apiKey : "";
	}

	public boolean isApiKeyRequired() {
		return apiKeyRequired;
	}

	public void setApiKeyRequired(boolean apiKeyRequired) {
		this.apiKeyRequired = apiKeyRequired;
	}

	public List<StaticSite> getStaticSites() {
		return staticSites;
	}

	public void setStaticSites(List<StaticSite> staticSites) {
		this.staticSites = staticSites != null ? staticSites : new ArrayList<>();
	}

	public static class StaticSite {

		/**
		 * URL segment after {@code /proxy/} for this site (no slashes).
		 */
		private String route;

		/**
		 * Directory on disk whose files are served for this route (bound as string, exposed as {@link Path}).
		 */
		private String staticFiles;

		/**
		 * Event {@code action} sent when a page is loaded (must match analytics queries; {@code page_visit} is typical).
		 */
		private String analyticsName;

		/**
		 * Registered service id events are posted to.
		 */
		private UUID serviceId;

		/**
		 * If set, Tomcat listens on {@code 127.0.0.1:thisPort} and serves this site at URL path {@code /} (same as
		 * {@code /proxy/{route}/} on the main server port). Must be unique across sites and not equal to {@code server.port}.
		 */
		private Integer localhostPort;

		public String getRoute() {
			return route;
		}

		public void setRoute(String route) {
			this.route = route;
		}

		public Path getStaticFiles() {
			return staticFiles == null || staticFiles.isBlank() ? null : Path.of(staticFiles.trim());
		}

		public void setStaticFiles(String staticFiles) {
			this.staticFiles = staticFiles;
		}

		public String getAnalyticsName() {
			return analyticsName;
		}

		public void setAnalyticsName(String analyticsName) {
			this.analyticsName = analyticsName;
		}

		public UUID getServiceId() {
			return serviceId;
		}

		public void setServiceId(UUID serviceId) {
			this.serviceId = serviceId;
		}

		public Integer getLocalhostPort() {
			return localhostPort;
		}

		public void setLocalhostPort(Integer localhostPort) {
			this.localhostPort = localhostPort;
		}
	}
}
