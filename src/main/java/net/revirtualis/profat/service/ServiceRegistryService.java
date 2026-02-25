package net.revirtualis.profat.service;

import net.revirtualis.profat.dto.ServiceCreateRequest;
import net.revirtualis.profat.dto.ServiceResponse;
import net.revirtualis.profat.entity.RegisteredService;
import net.revirtualis.profat.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServiceRegistryService {

	private final ServiceRepository serviceRepository;

	public ServiceRegistryService(ServiceRepository serviceRepository) {
		this.serviceRepository = serviceRepository;
	}

	@Transactional
	public ServiceResponse create(ServiceCreateRequest request) {
		UUID id = UUID.randomUUID();
		String createdAt = Instant.now().toString();
		RegisteredService entity = new RegisteredService(id, request.getName().trim(), createdAt);
		serviceRepository.save(entity);
		return new ServiceResponse(entity.getId(), entity.getName(), entity.getCreatedAt());
	}

	public List<ServiceResponse> listAll() {
		return serviceRepository.findAll().stream()
				.map(s -> new ServiceResponse(s.getId(), s.getName(), s.getCreatedAt()))
				.collect(Collectors.toList());
	}

	public boolean existsById(UUID serviceId) {
		return serviceRepository.existsById(serviceId);
	}
}
