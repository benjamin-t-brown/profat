package net.revirtualis.profat.controller;

import net.revirtualis.profat.StaticSiteTestConfig;
import net.revirtualis.profat.config.ApiKeyAuthenticationFilter;
import net.revirtualis.profat.config.ProfatProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(StaticSiteTestConfig.class)
class StaticSiteIntegrationTest {

	private static final Path siteRoot;

	static {
		try {
			siteRoot = Files.createTempDirectory("profat-static-int");
			Files.writeString(
					siteRoot.resolve("index.html"),
					"<!DOCTYPE html><html><head><title>t</title>"
							+ "<link rel=\"stylesheet\" href=\"/style.css\"></head><body>ok</body></html>",
					StandardCharsets.UTF_8);
			Files.writeString(siteRoot.resolve("style.css"), "body{background:url(/dot.png)}", StandardCharsets.UTF_8);
			Files.write(siteRoot.resolve("dot.png"), new byte[] {(byte) 0x89});
			Files.createDirectories(siteRoot.resolve("sub"));
			Files.writeString(siteRoot.resolve("sub/page.html"), "<html><body>sub</body></html>", StandardCharsets.UTF_8);
			Files.createDirectories(siteRoot.resolve("game-posts"));
			Files.writeString(
					siteRoot.resolve("game-posts/regem-ludos.html"),
					"<html><body>regem</body></html>",
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@DynamicPropertySource
	static void registerProps(DynamicPropertyRegistry r) {
		String root = siteRoot.toAbsolutePath().toString().replace('\\', '/');
		r.add("profat.staticSites[0].route", () -> "demosite");
		r.add("profat.staticSites[0].staticFiles", () -> root);
		r.add("profat.staticSites[0].analyticsName", () -> "page_visit");
		r.add("profat.staticSites[0].serviceId", () -> "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
	}

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ProfatProperties profatProperties;

	@Test
	void staticSiteConfigurationIsBound() {
		assertThat(profatProperties.getStaticSites()).hasSize(1);
		assertThat(profatProperties.getStaticSites().get(0).getRoute()).isEqualTo("demosite");
		assertThat(profatProperties.getStaticSites().get(0).getStaticFiles()).isNotNull();
	}

	@Test
	void getIndex_logsPageVisitServerSide_andServesHtmlUnchanged() throws Exception {
		mvc.perform(get("/proxy/demosite"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
				.andExpect(content().string(containsString("ok")))
				.andExpect(content().string(containsString("href=\"/style.css\"")));
		mvc.perform(get("/api/v1/services/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee/events")
						.param("page", "0")
						.param("size", "10")
						.header(ApiKeyAuthenticationFilter.HEADER_NAME, "test-key"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].action").value("page_visit"));
	}

	@Test
	void getNestedFile_servesHtml() throws Exception {
		mvc.perform(get("/proxy/demosite/sub/page.html"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("sub")));
	}

	@Test
	void getCss_servesFileUnchanged() throws Exception {
		mvc.perform(get("/proxy/demosite/style.css"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/css"))
				.andExpect(content().string(containsString("url(/dot.png)")));
	}

	@Test
	void getBinaryAsset_servesWithoutTransform() throws Exception {
		mvc.perform(get("/proxy/demosite/dot.png"))
				.andExpect(status().isOk());
	}

	@Test
	void extensionlessPath_resolvesHtmlSibling() throws Exception {
		mvc.perform(get("/proxy/demosite/game-posts/regem-ludos"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("regem")));
	}

	@Test
	void unknownRoute_returns404() throws Exception {
		mvc.perform(get("/proxy/unknown-route"))
				.andExpect(status().isNotFound());
	}
}
