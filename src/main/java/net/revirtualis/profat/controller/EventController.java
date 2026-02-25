package net.revirtualis.profat.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import net.revirtualis.profat.dto.*;
import net.revirtualis.profat.service.AnalyticsService;
import net.revirtualis.profat.service.EventService;
import net.revirtualis.profat.service.ServiceRegistryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services/{serviceId}")
public class EventController {

	private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

	private final ServiceRegistryService serviceRegistryService;
	private final EventService eventService;
	private final AnalyticsService analyticsService;

	public EventController(ServiceRegistryService serviceRegistryService,
						   EventService eventService,
						   AnalyticsService analyticsService) {
		this.serviceRegistryService = serviceRegistryService;
		this.eventService = eventService;
		this.analyticsService = analyticsService;
	}

	@PostMapping("/events")
	public ResponseEntity<EventResponse> logEvent(
			@PathVariable UUID serviceId,
			@Valid @RequestBody EventCreateRequest request,
			HttpServletRequest httpRequest) {
		if (!serviceRegistryService.existsById(serviceId)) {
			return ResponseEntity.notFound().build();
		}
		String clientIp = resolveClientIp(httpRequest);
		EventResponse created = eventService.create(serviceId, request, clientIp);
		return ResponseEntity.status(201).body(created);
	}

	@GetMapping("/events")
	public ResponseEntity<Page<EventListItem>> listEvents(
			@PathVariable UUID serviceId,
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (!serviceRegistryService.existsById(serviceId)) {
			return ResponseEntity.notFound().build();
		}
		Pageable pageable = PageRequest.of(page, Math.min(size, 100));
		Page<EventListItem> result = eventService.listEvents(serviceId, search, pageable);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/actions")
	public ResponseEntity<List<String>> listActions(@PathVariable UUID serviceId) {
		if (!serviceRegistryService.existsById(serviceId)) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(eventService.listActions(serviceId));
	}

	@GetMapping("/analytics/summary")
	public ResponseEntity<AnalyticsSummary> getSummary(
			@PathVariable UUID serviceId,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to) {
		if (!serviceRegistryService.existsById(serviceId)) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(analyticsService.getSummary(serviceId, from, to));
	}

	private static String resolveClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader(HEADER_X_FORWARDED_FOR);
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		String remote = request.getRemoteAddr();
		return remote != null ? remote : "";
	}
}
