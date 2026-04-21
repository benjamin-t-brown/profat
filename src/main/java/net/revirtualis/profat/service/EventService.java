package net.revirtualis.profat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.revirtualis.profat.dto.EventCreateRequest;
import net.revirtualis.profat.dto.EventListItem;
import net.revirtualis.profat.dto.EventResponse;
import net.revirtualis.profat.entity.Event;
import net.revirtualis.profat.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

	private final EventRepository eventRepository;
	private final ObjectMapper objectMapper;
	private final PageVisitActionCatalog pageVisitActionCatalog;

	public EventService(EventRepository eventRepository, ObjectMapper objectMapper, PageVisitActionCatalog pageVisitActionCatalog) {
		this.eventRepository = eventRepository;
		this.objectMapper = objectMapper;
		this.pageVisitActionCatalog = pageVisitActionCatalog;
	}

	@Transactional
	public EventResponse create(UUID serviceId, EventCreateRequest request, String clientIp) {
		UUID id = UUID.randomUUID();
		String createdAt = Instant.now().toString();
		String payloadJson = serializePayload(request.getPayload());

		Event event = new Event();
		event.setId(id);
		event.setServiceId(serviceId);
		event.setAction(request.getAction());
		event.setPayload(payloadJson);
		event.setCreatedAt(createdAt);

		if (pageVisitActionCatalog.isPageVisitLike(request.getAction()) && request.getPayload() != null) {
			event.setPageUrl(getString(request.getPayload(), "pageUrl"));
			event.setCountry(getString(request.getPayload(), "country"));
			event.setIsMobile(getBoolean(request.getPayload(), "isMobile"));
			event.setIp(clientIp);
		}

		eventRepository.save(event);
		return new EventResponse(event.getId(), event.getAction(), event.getCreatedAt());
	}

	public Page<EventListItem> listEvents(UUID serviceId, String search, Pageable pageable) {
		Page<Event> page = search != null && !search.isBlank()
				? eventRepository.findByServiceIdAndSearch(serviceId, search.trim(), pageable)
				: eventRepository.findByServiceIdOrderByCreatedAtDesc(serviceId, pageable);
		return page.map(e -> new EventListItem(e.getId(), e.getAction(), e.getCreatedAt(), e.getPayload()));
	}

	public List<String> listActions(UUID serviceId) {
		return eventRepository.findDistinctActionByServiceId(serviceId);
	}

	private static String getString(java.util.Map<String, Object> payload, String key) {
		Object v = payload.get(key);
		return v == null ? null : v.toString();
	}

	// @SuppressWarnings("unchecked")
	private static Boolean getBoolean(java.util.Map<String, Object> payload, String key) {
		Object v = payload.get(key);
		if (v == null) {
			return null;
		}
		if (v instanceof Boolean b) {
			return b;
		}
		if (v instanceof String s) {
			return Boolean.parseBoolean(s);
		}
		return null;
	}

	private String serializePayload(java.util.Map<String, Object> payload) {
		if (payload == null || payload.isEmpty())
			return "{}";
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			return "{}";
		}
	}
}
