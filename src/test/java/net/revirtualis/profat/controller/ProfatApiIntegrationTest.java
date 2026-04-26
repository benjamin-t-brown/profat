package net.revirtualis.profat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.revirtualis.profat.config.ApiKeyAuthenticationFilter;
import net.revirtualis.profat.dto.EventCreateRequest;
import net.revirtualis.profat.dto.ServiceCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProfatApiIntegrationTest {

	private static final String TEST_API_KEY = "test-key";

	private static RequestPostProcessor apiKey() {
		return request -> {
			request.addHeader(ApiKeyAuthenticationFilter.HEADER_NAME, TEST_API_KEY);
			return request;
		};
	}

	/** Avoid duplicate-name failures when reusing a persistent SQLite file across test runs. */
	private static String uniqueName(String stem) {
		return stem + "-" + UUID.randomUUID();
	}

	@Test
	void profatPath_forwardsToApiTesterIndex() throws Exception {
		mvc.perform(get("/profat"))
				.andExpect(status().isOk())
				.andExpect(forwardedUrl("/index.html"));
		mvc.perform(get("/profat/"))
				.andExpect(status().isOk())
				.andExpect(forwardedUrl("/index.html"));
		mvc.perform(get("/profat/deep/path"))
				.andExpect(status().isOk())
				.andExpect(forwardedUrl("/index.html"));
	}

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void apiWithoutKey_returns401() throws Exception {
		mvc.perform(get("/api/v1/services"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.error").value("Unauthorized"));
	}

	@Test
	void apiWithWrongKey_returns401() throws Exception {
		mvc.perform(get("/api/v1/services").header(ApiKeyAuthenticationFilter.HEADER_NAME, "wrong-key"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void createService_andListServices() throws Exception {
		ServiceCreateRequest create = new ServiceCreateRequest();
		create.setName(uniqueName("App A"));
		mvc.perform(post("/api/v1/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(create))
						.with(apiKey()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.name").isString())
				.andExpect(jsonPath("$.createdAt").isString());

		mvc.perform(get("/api/v1/services").with(apiKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
	}

	@Test
	void createService_duplicateName_returns409() throws Exception {
		String canonical = "Unique Name X " + UUID.randomUUID();
		ServiceCreateRequest first = new ServiceCreateRequest();
		first.setName(canonical);
		mvc.perform(post("/api/v1/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(first))
						.with(apiKey()))
				.andExpect(status().isCreated());

		ServiceCreateRequest duplicate = new ServiceCreateRequest();
		duplicate.setName(canonical.toLowerCase(Locale.ROOT));
		mvc.perform(post("/api/v1/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(duplicate))
						.with(apiKey()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.error").value("Conflict"))
				.andExpect(jsonPath("$.message").value("A service with this name already exists"));
	}

	@Test
	void postPageVisitAndCustomAction_thenListEventsAndActions_andGetSummary() throws Exception {
		// Create service
		ServiceCreateRequest create = new ServiceCreateRequest();
		create.setName(uniqueName("Test App"));
		String serviceJson = mvc.perform(post("/api/v1/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(create))
						.with(apiKey()))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String serviceId = objectMapper.readTree(serviceJson).get("id").asText();

		// POST page_visit events for two IPs to test >10/day filtering
		EventCreateRequest pageVisit = new EventCreateRequest();
		pageVisit.setAction("page_visit");
		Map<String, Object> visitPayload = new LinkedHashMap<>();
		visitPayload.put("pageUrl", "/home");
		visitPayload.put("country", "US");
		visitPayload.put("isMobile", false);
		visitPayload.put("userAgent", "Mozilla/5.0");
		pageVisit.setPayload(visitPayload);
		for (int i = 0; i < 11; i++) {
			mvc.perform(post("/api/v1/services/" + serviceId + "/events")
							.header("X-Forwarded-For", "203.0.113.10")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(pageVisit))
							.with(apiKey()))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.action").value("page_visit"))
					.andExpect(jsonPath("$.createdAt").exists());
		}
		for (int i = 0; i < 10; i++) {
			mvc.perform(post("/api/v1/services/" + serviceId + "/events")
							.header("X-Forwarded-For", "203.0.113.11")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(pageVisit))
							.with(apiKey()))
					.andExpect(status().isCreated());
		}

		// POST custom action
		EventCreateRequest signup = new EventCreateRequest();
		signup.setAction("signup");
		signup.setPayload(Map.of("plan", "pro"));
		mvc.perform(post("/api/v1/services/" + serviceId + "/events")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(signup))
						.with(apiKey()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.action").value("signup"));

		// GET events (paginated)
		mvc.perform(get("/api/v1/services/" + serviceId + "/events").param("page", "0").param("size", "10")
						.with(apiKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(10)))
				.andExpect(jsonPath("$.totalElements").value(22));

		// GET events with search
		mvc.perform(get("/api/v1/services/" + serviceId + "/events").param("search", "pro")
						.with(apiKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));

		// GET actions
		mvc.perform(get("/api/v1/services/" + serviceId + "/actions").with(apiKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasItems("page_visit", "signup")));

		// GET analytics summary (use today UTC so the event we just created is included)
		String today = LocalDate.now(ZoneOffset.UTC).toString();
		mvc.perform(get("/api/v1/services/" + serviceId + "/analytics/summary")
						.param("from", today).param("to", today)
						.with(apiKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalPageLoads").value(21))
				.andExpect(jsonPath("$.uniqueCountries", hasItem("US")))
				.andExpect(jsonPath("$.visitsPerHour").isArray())
				.andExpect(jsonPath("$.visitsPerHour[0].hour", containsString("T")))
				.andExpect(jsonPath("$.visitsPerHour[0].hour", endsWith(":00:00Z")))
				.andExpect(jsonPath("$.visitsPerHour[0].count", greaterThanOrEqualTo(1)))
				.andExpect(jsonPath("$.ipVisitsPerDay").isArray())
				.andExpect(jsonPath("$.ipVisitsPerDay[0].ips[0].ip").value("203.0.113.10"))
				.andExpect(jsonPath("$.ipVisitsPerDay[0].ips[0].count").value(11))
				.andExpect(jsonPath("$.ipVisitsPerDay[0].ips[*].ip", not(hasItem("203.0.113.11"))))
				.andExpect(jsonPath("$.pageVisitsByIp").doesNotExist())
				.andExpect(jsonPath("$.pageLoadsByDevice.mobile").exists())
				.andExpect(jsonPath("$.pageLoadsByDevice.desktop").exists())
				.andExpect(jsonPath("$.visitsPerDay").isArray());
	}

	@Test
	void unknownServiceId_returns404() throws Exception {
		String unknownId = "00000000-0000-0000-0000-000000000000";
		mvc.perform(get("/api/v1/services/" + unknownId + "/events").with(apiKey()))
				.andExpect(status().isNotFound());
		mvc.perform(get("/api/v1/services/" + unknownId + "/actions").with(apiKey()))
				.andExpect(status().isNotFound());
		mvc.perform(get("/api/v1/services/" + unknownId + "/analytics/summary").with(apiKey()))
				.andExpect(status().isNotFound());
		mvc.perform(delete("/api/v1/services/" + unknownId).with(apiKey()))
				.andExpect(status().isNotFound());
	}

	@Test
	void invalidServiceId_returns400() throws Exception {
		mvc.perform(get("/api/v1/services/not-a-uuid/events").with(apiKey()))
				.andExpect(status().isBadRequest());
		mvc.perform(delete("/api/v1/services/not-a-uuid").with(apiKey()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deleteService_removesService_andEvents() throws Exception {
		ServiceCreateRequest create = new ServiceCreateRequest();
		create.setName(uniqueName("To Delete"));
		String serviceJson = mvc.perform(post("/api/v1/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(create))
						.with(apiKey()))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String serviceId = objectMapper.readTree(serviceJson).get("id").asText();

		EventCreateRequest ev = new EventCreateRequest();
		ev.setAction("ping");
		ev.setPayload(Map.of());
		mvc.perform(post("/api/v1/services/" + serviceId + "/events")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(ev))
						.with(apiKey()))
				.andExpect(status().isCreated());

		mvc.perform(delete("/api/v1/services/" + serviceId).with(apiKey()))
				.andExpect(status().isNoContent());

		mvc.perform(get("/api/v1/services/" + serviceId + "/events").with(apiKey()))
				.andExpect(status().isNotFound());
		mvc.perform(get("/api/v1/services").with(apiKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", not(hasItem(serviceId))));
	}

	@Test
	void createEvent_withBlankAction_returns400() throws Exception {
		ServiceCreateRequest create = new ServiceCreateRequest();
		create.setName(uniqueName("Val App"));
		String serviceJson = mvc.perform(post("/api/v1/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(create))
						.with(apiKey()))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String serviceId = objectMapper.readTree(serviceJson).get("id").asText();

		EventCreateRequest bad = new EventCreateRequest();
		bad.setAction(" ");
		bad.setPayload(Map.of());
		mvc.perform(post("/api/v1/services/" + serviceId + "/events")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(bad))
						.with(apiKey()))
				.andExpect(status().isBadRequest());
	}
}
