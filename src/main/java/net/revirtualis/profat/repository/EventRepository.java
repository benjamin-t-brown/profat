package net.revirtualis.profat.repository;

import net.revirtualis.profat.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

	void deleteByServiceId(UUID serviceId);

	Page<Event> findByServiceIdOrderByCreatedAtDesc(UUID serviceId, Pageable pageable);

	@Query("SELECT e FROM Event e WHERE e.serviceId = :serviceId AND (LOWER(e.payload) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.action) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY e.createdAt DESC")
	Page<Event> findByServiceIdAndSearch(@Param("serviceId") UUID serviceId, @Param("search") String search, Pageable pageable);

	@Query("SELECT DISTINCT e.action FROM Event e WHERE e.serviceId = :serviceId ORDER BY e.action")
	List<String> findDistinctActionByServiceId(@Param("serviceId") UUID serviceId);

	// Analytics: page-visit-like actions (page_visit + configured static-site analytics names)
	@Query(value = "SELECT substr(e.created_at, 1, 10) AS day, COUNT(*) FROM event e WHERE e.service_id = :serviceId AND e.action IN (:actions) AND e.created_at >= :fromDate AND e.created_at <= :toDate GROUP BY day ORDER BY day", nativeQuery = true)
	List<Object[]> countPageVisitsByDay(@Param("serviceId") UUID serviceId, @Param("actions") List<String> actions, @Param("fromDate") String fromDate, @Param("toDate") String toDate);

	@Query("SELECT DISTINCT e.country FROM Event e WHERE e.serviceId = :serviceId AND e.action IN :actions AND e.country IS NOT NULL AND e.createdAt >= :fromDate AND e.createdAt <= :toDate ORDER BY e.country")
	List<String> findDistinctCountriesForPageVisits(@Param("serviceId") UUID serviceId, @Param("actions") List<String> actions, @Param("fromDate") String fromDate, @Param("toDate") String toDate);

	@Query("SELECT COUNT(e) FROM Event e WHERE e.serviceId = :serviceId AND e.action IN :actions AND e.createdAt >= :fromDate AND e.createdAt <= :toDate")
	long countPageVisits(@Param("serviceId") UUID serviceId, @Param("actions") List<String> actions, @Param("fromDate") String fromDate, @Param("toDate") String toDate);

	@Query("SELECT COUNT(e) FROM Event e WHERE e.serviceId = :serviceId AND e.action IN :actions AND e.isMobile = true AND e.createdAt >= :fromDate AND e.createdAt <= :toDate")
	long countPageVisitsMobile(@Param("serviceId") UUID serviceId, @Param("actions") List<String> actions, @Param("fromDate") String fromDate, @Param("toDate") String toDate);

	@Query("SELECT COUNT(e) FROM Event e WHERE e.serviceId = :serviceId AND e.action IN :actions AND (e.isMobile = false OR e.isMobile IS NULL) AND e.createdAt >= :fromDate AND e.createdAt <= :toDate")
	long countPageVisitsDesktop(@Param("serviceId") UUID serviceId, @Param("actions") List<String> actions, @Param("fromDate") String fromDate, @Param("toDate") String toDate);

	@Query(value = "SELECT json_extract(e.payload, '$.ip') AS ip, COUNT(*) FROM event e WHERE e.service_id = :serviceId AND e.action IN (:actions) AND e.created_at >= :fromDate AND e.created_at <= :toDate AND json_extract(e.payload, '$.ip') IS NOT NULL AND trim(cast(json_extract(e.payload, '$.ip') AS text)) != '' GROUP BY json_extract(e.payload, '$.ip') ORDER BY COUNT(*) DESC, ip ASC", nativeQuery = true)
	List<Object[]> countPageVisitsByPayloadIp(@Param("serviceId") UUID serviceId, @Param("actions") List<String> actions, @Param("fromDate") String fromDate, @Param("toDate") String toDate);
}
