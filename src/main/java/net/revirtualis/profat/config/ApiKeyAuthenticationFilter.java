package net.revirtualis.profat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.revirtualis.profat.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Requires the {@code X-Profat-Key} header to match {@link ProfatProperties#getApiKey()} for requests under {@code /api/v1}.
 * When the configured key is blank, this filter does nothing.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

	public static final String HEADER_NAME = "X-Profat-Key";

	private final ProfatProperties profatProperties;
	private final ObjectMapper objectMapper;

	public ApiKeyAuthenticationFilter(ProfatProperties profatProperties, ObjectMapper objectMapper) {
		this.profatProperties = profatProperties;
		this.objectMapper = objectMapper;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String key = profatProperties.getApiKey();
		if (key == null || key.isBlank()) {
			return true;
		}
		String path = pathWithinApplication(request);
		return !path.startsWith("/api/v1");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String expected = profatProperties.getApiKey();
		String provided = request.getHeader(HEADER_NAME);
		if (!constantTimeEquals(expected, provided)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			ErrorResponse body = new ErrorResponse(
					HttpStatus.UNAUTHORIZED.value(),
					"Unauthorized",
					"Invalid or missing " + HEADER_NAME + " header");
			objectMapper.writeValue(response.getOutputStream(), body);
			return;
		}
		filterChain.doFilter(request, response);
	}

	private static String pathWithinApplication(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
			uri = uri.substring(ctx.length());
		}
		if (uri.isEmpty()) {
			return "/";
		}
		return uri.startsWith("/") ? uri : "/" + uri;
	}

	private static boolean constantTimeEquals(String expected, String provided) {
		if (expected == null || provided == null) {
			return false;
		}
		byte[] a = expected.getBytes(StandardCharsets.UTF_8);
		byte[] b = provided.getBytes(StandardCharsets.UTF_8);
		if (a.length != b.length) {
			return false;
		}
		return MessageDigest.isEqual(a, b);
	}
}
