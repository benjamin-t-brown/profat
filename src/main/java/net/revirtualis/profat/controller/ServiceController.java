package net.revirtualis.profat.controller;

import jakarta.validation.Valid;
import net.revirtualis.profat.dto.ServiceCreateRequest;
import net.revirtualis.profat.dto.ServiceResponse;
import net.revirtualis.profat.service.ServiceRegistryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

	private final ServiceRegistryService serviceRegistryService;

	public ServiceController(ServiceRegistryService serviceRegistryService) {
		this.serviceRegistryService = serviceRegistryService;
	}

	@PostMapping
	public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceCreateRequest request) {
		ServiceResponse created = serviceRegistryService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping
	public List<ServiceResponse> list() {
		return serviceRegistryService.listAll();
	}
}
