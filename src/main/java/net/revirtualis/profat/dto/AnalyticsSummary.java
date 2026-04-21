package net.revirtualis.profat.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsSummary {

	private List<Map<String, Object>> visitsPerDay;
	private List<String> uniqueCountries;
	private List<Map<String, Object>> pageVisitsByIp;
	private long totalPageLoads;
	private Map<String, Long> pageLoadsByDevice;

	public AnalyticsSummary() {
	}

	public List<Map<String, Object>> getVisitsPerDay() {
		return visitsPerDay;
	}

	public void setVisitsPerDay(List<Map<String, Object>> visitsPerDay) {
		this.visitsPerDay = visitsPerDay;
	}

	public List<String> getUniqueCountries() {
		return uniqueCountries;
	}

	public void setUniqueCountries(List<String> uniqueCountries) {
		this.uniqueCountries = uniqueCountries;
	}

	public List<Map<String, Object>> getPageVisitsByIp() {
		return pageVisitsByIp;
	}

	public void setPageVisitsByIp(List<Map<String, Object>> pageVisitsByIp) {
		this.pageVisitsByIp = pageVisitsByIp;
	}

	public long getTotalPageLoads() {
		return totalPageLoads;
	}

	public void setTotalPageLoads(long totalPageLoads) {
		this.totalPageLoads = totalPageLoads;
	}

	public Map<String, Long> getPageLoadsByDevice() {
		return pageLoadsByDevice;
	}

	public void setPageLoadsByDevice(Map<String, Long> pageLoadsByDevice) {
		this.pageLoadsByDevice = pageLoadsByDevice;
	}
}
