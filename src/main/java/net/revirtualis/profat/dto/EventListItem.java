package net.revirtualis.profat.dto;

import java.util.UUID;

public class EventListItem {

	private UUID id;
	private String action;
	private String createdAt;
	private String payload;

	public EventListItem() {
	}

	public EventListItem(UUID id, String action, String createdAt, String payload) {
		this.id = id;
		this.action = action;
		this.createdAt = createdAt;
		this.payload = payload;
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

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
}
