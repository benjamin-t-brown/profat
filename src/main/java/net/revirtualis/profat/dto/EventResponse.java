package net.revirtualis.profat.dto;

import java.util.UUID;

public class EventResponse {

	private UUID id;
	private String action;
	private String createdAt;

	public EventResponse() {
	}

	public EventResponse(UUID id, String action, String createdAt) {
		this.id = id;
		this.action = action;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
}
