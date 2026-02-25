package net.revirtualis.profat.repository;

import net.revirtualis.profat.entity.RegisteredService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServiceRepository extends JpaRepository<RegisteredService, UUID> {
}
