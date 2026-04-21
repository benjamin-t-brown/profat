package net.revirtualis.profat.service;

import net.revirtualis.profat.dto.ServiceCreateRequest;
import net.revirtualis.profat.dto.ServiceResponse;
import net.revirtualis.profat.entity.RegisteredService;
import net.revirtualis.profat.exception.DuplicateServiceNameException;
import net.revirtualis.profat.repository.EventRepository;
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
	private final EventRepository eventRepository;

	public ServiceRegistryService(ServiceRepository serviceRepository, EventRepository eventRepository) {
		this.serviceRepository = serviceRepository;
		this.eventRepository = eventRepository;
	}

	@Transactional
	public ServiceResponse create(ServiceCreateRequest request) {
		String name = request.getName().trim();
		if (serviceRepository.existsByNameIgnoreCase(name)) {
			throw new DuplicateServiceNameException("A service with this name already exists");
		}
		UUID id = UUID.randomUUID();
		String createdAt = Instant.now().toString();
		RegisteredService entity = new RegisteredService(id, name, createdAt);
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

	@Transactional
	public boolean delete(UUID serviceId) {
		if (!serviceRepository.existsById(serviceId)) {
			return false;
		}
		eventRepository.deleteByServiceId(serviceId);
		serviceRepository.deleteById(serviceId);
		return true;
	}
}
