package net.revirtualis.profat.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.revirtualis.profat.config.ProfatProperties;
import net.revirtualis.profat.dto.EventCreateRequest;
import net.revirtualis.profat.service.EventService;
import net.revirtualis.profat.service.StaticSiteRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@RestController
public class StaticSiteController {

	private static final Logger log = LoggerFactory.getLogger(StaticSiteController.class);
	private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
	private static final Pattern MOBILE_UA = Pattern.compile("(?i).*(Mobi|Android).*");

	private final StaticSiteRegistry staticSiteRegistry;
	private final EventService eventService;

	public StaticSiteController(StaticSiteRegistry staticSiteRegistry, EventService eventService) {
		this.staticSiteRegistry = staticSiteRegistry;
		this.eventService = eventService;
	}

	@RequestMapping(value = {"/proxy/{route}", "/proxy/{route}/**"}, method = {RequestMethod.GET, RequestMethod.HEAD})
	public void serve(
			@PathVariable String route,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// log.info("Proxy: {} {}", request.getMethod(), requestUriWithQuery(request));
		Optional<ProfatProperties.StaticSite> siteOpt = staticSiteRegistry.findByRoute(route);
		if (siteOpt.isEmpty()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		ProfatProperties.StaticSite site = siteOpt.get();
		String rest = normalizeRemainder(pathAfterRoute(request, route));
		Path root = site.getStaticFiles().toAbsolutePath().normalize();
		Path resolved = tryFilesResolve(root, rest);
		if (resolved == null) {
			log.warn(
					"Static site not resolved: route='{}' remainder='{}' root='{}' requestUri='{}'",
					route,
					rest.isEmpty() ? "/" : rest,
					root,
					request.getRequestURI());
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String contentType = resolveContentType(resolved);
		setContentTypeWithCharset(response, contentType);
		response.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic().getHeaderValue());

		boolean isHead = RequestMethod.HEAD.name().equalsIgnoreCase(request.getMethod());
		if (isHead) {
			response.setContentLengthLong(Files.size(resolved));
			return;
		}

		if (isHtmlContent(contentType, resolved)) {
			logHtmlPageView(site, request);
		}

		response.setContentLengthLong(Files.size(resolved));
		try (OutputStream out = response.getOutputStream(); var in = Files.newInputStream(resolved)) {
			in.transferTo(out);
		}
	}

	private static void setContentTypeWithCharset(HttpServletResponse response, String contentType) {
		if (contentType.startsWith("text/") || contentType.contains("javascript") || contentType.contains("json")
				|| contentType.contains("xml")) {
			response.setContentType(contentType + ";charset=UTF-8");
		} else {
			response.setContentType(contentType);
		}
	}

	private static boolean isHtmlContent(String contentType, Path path) {
		if (contentType.startsWith(MediaType.TEXT_HTML_VALUE)) {
			return true;
		}
		String n = path.getFileName().toString().toLowerCase();
		return n.endsWith(".html") || n.endsWith(".htm");
	}

	/**
	 * Prefer extension-based detection (reliable on Windows) then {@link Files#probeContentType}.
	 */
	private static String resolveContentType(Path resolved) throws IOException {
		String name = resolved.getFileName().toString();
		Optional<MediaType> byName = MediaTypeFactory.getMediaType(name);
		if (byName.isPresent()) {
			return byName.get().toString();
		}
		String probed = Files.probeContentType(resolved);
		if (probed != null && !probed.isBlank()) {
			return probed;
		}
		return MediaType.APPLICATION_OCTET_STREAM_VALUE;
	}

	/**
	 * Records a page-visit event from the incoming HTTP request (same pipeline as POST /events).
	 */
	private void logHtmlPageView(ProfatProperties.StaticSite site, HttpServletRequest request) {
		try {
			String clientIp = resolveClientIp(request);
			EventCreateRequest event = new EventCreateRequest();
			event.setAction(site.getAnalyticsName());
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put("pageUrl", requestUriWithQuery(request));
			if (!clientIp.isBlank()) {
				payload.put("ip", clientIp);
			}
			String ua = request.getHeader(HttpHeaders.USER_AGENT);
			if (ua != null && !ua.isBlank()) {
				payload.put("userAgent", ua);
			}
			payload.put("isMobile", MOBILE_UA.matcher(ua != null ? ua : "").find());
			event.setPayload(payload);
			eventService.create(site.getServiceId(), event, clientIp);
		} catch (Exception e) {
			log.warn("Failed to log static HTML page view: {}", e.toString());
		}
	}

	private static String requestUriWithQuery(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String q = request.getQueryString();
		if (q != null && !q.isBlank()) {
			return uri + "?" + q;
		}
		return uri;
	}

	private static String resolveClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader(HEADER_X_FORWARDED_FOR);
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		String remote = request.getRemoteAddr();
		return remote != null ? remote : "";
	}

	/**
	 * Path segments after {@code /proxy/{route}/} (empty string for the route root). Works with MockMvc and servlet containers.
	 */
	private static String pathAfterRoute(HttpServletRequest request, String route) {
		String context = request.getContextPath();
		String uri = request.getRequestURI();
		String path = uri.startsWith(context) ? uri.substring(context.length()) : uri;
		String prefix = "/proxy/" + route;
		if (!path.startsWith(prefix)) {
			return "";
		}
		String tail = path.substring(prefix.length());
		if (tail.startsWith("/")) {
			tail = tail.substring(1);
		}
		return tail;
	}

	/**
	 * Trims trailing slashes so {@code a/b/} maps like {@code a/b} for try_files.
	 */
	static String normalizeRemainder(String remainder) {
		if (remainder == null || remainder.isEmpty()) {
			return "";
		}
		String rel = remainder.replace('\\', '/').trim();
		if (rel.startsWith("/")) {
			rel = rel.substring(1);
		}
		while (rel.endsWith("/") && rel.length() > 1) {
			rel = rel.substring(0, rel.length() - 1);
		}
		return rel;
	}

	/**
	 * Maps URL remainder to a path under {@code root}, blocking path traversal (may point to a missing file).
	 */
	static Path resolveUnderRoot(Path root, String remainder) {
		String rel = normalizeRemainder(remainder);
		if (rel.isEmpty()) {
			return root;
		}
		Path candidate = root.resolve(rel).normalize();
		if (!candidate.startsWith(root)) {
			return null;
		}
		return candidate;
	}

	/**
	 * nginx {@code try_files}-style resolution: exact file, then {@code …/index.html} for a directory,
	 * then {@code {path}.html} next to the final segment (e.g. {@code /game-posts/regem-ludos} →
	 * {@code game-posts/regem-ludos.html}).
	 */
	static Path tryFilesResolve(Path root, String remainder) throws IOException {
		String rel = normalizeRemainder(remainder);
		Path base = resolveUnderRoot(root, rel);
		if (base == null) {
			return null;
		}
		if (rel.isEmpty()) {
			Path index = root.resolve("index.html").normalize();
			if (index.startsWith(root) && Files.isRegularFile(index)) {
				return index;
			}
			return null;
		}
		if (Files.isRegularFile(base)) {
			return base;
		}
		if (Files.isDirectory(base)) {
			Path index = base.resolve("index.html").normalize();
			if (index.startsWith(root) && Files.isRegularFile(index)) {
				return index;
			}
		}
		Path withHtml = base.resolveSibling(base.getFileName().toString() + ".html").normalize();
		if (withHtml.startsWith(root) && Files.isRegularFile(withHtml)) {
			return withHtml;
		}
		return null;
	}
}
