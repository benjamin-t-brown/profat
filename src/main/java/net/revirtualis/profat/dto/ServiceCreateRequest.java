package net.revirtualis.profat.dto;

import jakarta.validation.constraints.NotBlank;

public class ServiceCreateRequest {

	@NotBlank(message = "name must not be blank")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
