package net.revirtualis.profat.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "event", indexes = {
		@Index(name = "idx_event_service_created", columnList = "serviceId, createdAt"),
		@Index(name = "idx_event_action", columnList = "serviceId, action")
})
public class Event {

	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID serviceId;

	@Column(nullable = false)
	private String action;

	@Column(columnDefinition = "TEXT")
	private String payload;

	@Column(nullable = false)
	private String createdAt;

	// Optional columns for page_visit action (for efficient analytics)
	@Column(length = 2048)
	private String pageUrl;

	@Column(length = 64)
	private String country;

	private Boolean isMobile;

	@Column(length = 64)
	private String ip;

	public Event() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getServiceId() {
		return serviceId;
	}

	public void setServiceId(UUID serviceId) {
		this.serviceId = serviceId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Boolean getIsMobile() {
		return isMobile;
	}

	public void setIsMobile(Boolean isMobile) {
		this.isMobile = isMobile;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
