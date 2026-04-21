package net.revirtualis.profat.service;

import net.revirtualis.profat.dto.AnalyticsSummary;
import net.revirtualis.profat.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

	private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final int DEFAULT_DAYS = 30;

	private final EventRepository eventRepository;
	private final PageVisitActionCatalog pageVisitActionCatalog;

	public AnalyticsService(EventRepository eventRepository, PageVisitActionCatalog pageVisitActionCatalog) {
		this.eventRepository = eventRepository;
		this.pageVisitActionCatalog = pageVisitActionCatalog;
	}

	public AnalyticsSummary getSummary(java.util.UUID serviceId, String fromParam, String toParam) {
		LocalDate to = toParam != null && !toParam.isBlank()
				? LocalDate.parse(toParam.trim())
				: LocalDate.now();
		LocalDate from = fromParam != null && !fromParam.isBlank()
				? LocalDate.parse(fromParam.trim())
				: to.minusDays(DEFAULT_DAYS);
		if (from.isAfter(to)) {
			LocalDate swap = from;
			from = to;
			to = swap;
		}
		String fromStr = from.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
		String toStr = to.plusDays(1).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";

		List<Object[]> visitsByDay = eventRepository.countPageVisitsByDay(serviceId, pageVisitActionCatalog.listActions(), fromStr, toStr);
		List<Map<String, Object>> visitsPerDay = visitsByDay.stream()
				.map(row -> {
					Map<String, Object> map = new LinkedHashMap<>();
					map.put("date", row[0]);
					map.put("count", ((Number) row[1]).longValue());
					return map;
				})
				.collect(Collectors.toList());

		List<String> uniqueCountries = eventRepository.findDistinctCountriesForPageVisits(serviceId, pageVisitActionCatalog.listActions(), fromStr, toStr);
		List<Object[]> visitsByIpRows = eventRepository.countPageVisitsByPayloadIp(serviceId, pageVisitActionCatalog.listActions(), fromStr, toStr);
		List<Map<String, Object>> pageVisitsByIp = visitsByIpRows.stream()
				.map(row -> {
					Map<String, Object> map = new LinkedHashMap<>();
					map.put("ip", row[0]);
					map.put("count", ((Number) row[1]).longValue());
					return map;
				})
				.collect(Collectors.toList());
		long totalPageLoads = eventRepository.countPageVisits(serviceId, pageVisitActionCatalog.listActions(), fromStr, toStr);
		long mobile = eventRepository.countPageVisitsMobile(serviceId, pageVisitActionCatalog.listActions(), fromStr, toStr);
		long desktop = eventRepository.countPageVisitsDesktop(serviceId, pageVisitActionCatalog.listActions(), fromStr, toStr);

		Map<String, Long> pageLoadsByDevice = new LinkedHashMap<>();
		pageLoadsByDevice.put("mobile", mobile);
		pageLoadsByDevice.put("desktop", desktop);

		AnalyticsSummary summary = new AnalyticsSummary();
		summary.setVisitsPerDay(visitsPerDay);
		summary.setUniqueCountries(uniqueCountries);
		summary.setPageVisitsByIp(pageVisitsByIp);
		summary.setTotalPageLoads(totalPageLoads);
		summary.setPageLoadsByDevice(pageLoadsByDevice);
		return summary;
	}
}
