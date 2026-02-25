package net.revirtualis.profat.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class EventCreateRequest {

	@NotBlank(message = "action must not be blank")
	private String action;
	private Map<String, Object> payload;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}

	public void setPayload(Map<String, Object> payload) {
		this.payload = payload;
	}
}
