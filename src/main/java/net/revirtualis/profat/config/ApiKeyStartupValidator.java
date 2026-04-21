package net.revirtualis.profat.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Fails fast when API key auth is required but no key is configured.
 */
@Component
@Order(0)
public class ApiKeyStartupValidator implements ApplicationRunner {

	private final ProfatProperties profatProperties;

	public ApiKeyStartupValidator(ProfatProperties profatProperties) {
		this.profatProperties = profatProperties;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!profatProperties.isApiKeyRequired()) {
			return;
		}
		String key = profatProperties.getApiKey();
		if (key != null && !key.isBlank()) {
			return;
		}
		throw new IllegalStateException(
				"profat.api-key is required (profat.api-key-required=true) but is blank. "
						+ "Set environment variable PROFAT_API_KEY or property profat.api-key at deploy/runtime. "
						+ "Do not commit secrets; use gitignored application-local.properties for local dev only.");
	}
}
