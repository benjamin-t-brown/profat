package net.revirtualis.profat;

import net.revirtualis.profat.entity.RegisteredService;
import net.revirtualis.profat.repository.ServiceRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import java.time.Instant;
import java.util.UUID;

@TestConfiguration
public class StaticSiteTestConfig {

	@Bean
	@Order(40)
	ApplicationRunner seedStaticServiceForProxyTest(ServiceRepository serviceRepository) {
		return args -> {
			UUID id = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
			if (!serviceRepository.existsById(id)) {
				serviceRepository.save(new RegisteredService(id, "Proxy static test", Instant.now().toString()));
			}
		};
	}
}
