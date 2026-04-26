package net.revirtualis.profat.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsSummary {

	private List<Map<String, Object>> visitsPerDay;
	private List<Map<String, Object>> visitsPerHour;
	private List<String> uniqueCountries;
	private List<Map<String, Object>> ipVisitsPerDay;
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

	public List<Map<String, Object>> getVisitsPerHour() {
		return visitsPerHour;
	}

	public void setVisitsPerHour(List<Map<String, Object>> visitsPerHour) {
		this.visitsPerHour = visitsPerHour;
	}

	public List<Map<String, Object>> getIpVisitsPerDay() {
		return ipVisitsPerDay;
	}

	public void setIpVisitsPerDay(List<Map<String, Object>> ipVisitsPerDay) {
		this.ipVisitsPerDay = ipVisitsPerDay;
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
