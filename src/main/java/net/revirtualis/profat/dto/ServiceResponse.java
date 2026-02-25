package net.revirtualis.profat.dto;

import java.util.UUID;

public class ServiceResponse {

	private UUID id;
	private String name;
	private String createdAt;

	public ServiceResponse() {
	}

	public ServiceResponse(UUID id, String name, String createdAt) {
		this.id = id;
		this.name = name;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
}
