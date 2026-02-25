package net.revirtualis.profat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.revirtualis.profat.dto.EventCreateRequest;
import net.revirtualis.profat.dto.ServiceCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProfatApiIntegrationTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createService_andListServices() throws Exception {
		ServiceCreateRequest create = new ServiceCreateRequest();
		create.setName("App A");
		mvc.perform(post("/api/v1/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(create)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.name").isString())
				.andExpect(jsonPath("$.createdAt").isString());

		mvc.perform(get("/api/v1/services"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
	}

	@Test
	void postPageVisitAndCustomAction_thenListEventsAndActions_andGetSummary() throws Exception {
		// Create service
		ServiceCreateRequest create = new ServiceCreateRequest();
		create.setName("Test App");
		String serviceJson = mvc.perform(post("/api/v1/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(create)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String serviceId = objectMapper.readTree(serviceJson).get("id").asText();

		// POST page_visit event
		EventCreateRequest pageVisit = new EventCreateRequest();
		pageVisit.setAction("page_visit");
		pageVisit.setPayload(Map.of(
				"pageUrl", "/home",
				"country", "US",
				"isMobile", false,
				"userAgent", "Mozilla/5.0"));
		mvc.perform(post("/api/v1/services/" + serviceId + "/events")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(pageVisit)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.action").value("page_visit"))
				.andExpect(jsonPath("$.createdAt").exists());

		// POST custom action
		EventCreateRequest signup = new EventCreateRequest();
		signup.setAction("signup");
		signup.setPayload(Map.of("plan", "pro"));
		mvc.perform(post("/api/v1/services/" + serviceId + "/events")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(signup)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.action").value("signup"));

		// GET events (paginated)
		mvc.perform(get("/api/v1/services/" + serviceId + "/events").param("page", "0").param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.totalElements").value(2));

		// GET events with search
		mvc.perform(get("/api/v1/services/" + serviceId + "/events").param("search", "pro"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));

		// GET actions
		mvc.perform(get("/api/v1/services/" + serviceId + "/actions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasItems("page_visit", "signup")));

		// GET analytics summary (use today UTC so the event we just created is included)
		String today = LocalDate.now(ZoneOffset.UTC).toString();
		mvc.perform(get("/api/v1/services/" + serviceId + "/analytics/summary")
						.param("from", today).param("to", today))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalPageLoads").value(1))
				.andExpect(jsonPath("$.uniqueCountries", hasItem("US")))
				.andExpect(jsonPath("$.pageLoadsByDevice.mobile").exists())
				.andExpect(jsonPath("$.pageLoadsByDevice.desktop").exists())
				.andExpect(jsonPath("$.visitsPerDay").isArray());
	}

	@Test
	void unknownServiceId_returns404() throws Exception {
		String unknownId = "00000000-0000-0000-0000-000000000000";
		mvc.perform(get("/api/v1/services/" + unknownId + "/events"))
				.andExpect(status().isNotFound());
		mvc.perform(get("/api/v1/services/" + unknownId + "/actions"))
				.andExpect(status().isNotFound());
		mvc.perform(get("/api/v1/services/" + unknownId + "/analytics/summary"))
				.andExpect(status().isNotFound());
	}

	@Test
	void invalidServiceId_returns400() throws Exception {
		mvc.perform(get("/api/v1/services/not-a-uuid/events"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createEvent_withBlankAction_returns400() throws Exception {
		ServiceCreateRequest create = new ServiceCreateRequest();
		create.setName("Val App");
		String serviceJson = mvc.perform(post("/api/v1/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(create)))
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
						.content(objectMapper.writeValueAsString(bad)))
				.andExpect(status().isBadRequest());
	}
}
