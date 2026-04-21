package net.revirtualis.profat.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * When the request arrives on a configured extra connector port, forwards to the same handler as
 * {@code /proxy/{route}/...} so each static site can be mounted at the root of its own localhost port.
 */
public class LocalhostPortProxyFilter extends OncePerRequestFilter {

	private final Map<Integer, String> portToRoute;

	public LocalhostPortProxyFilter(Map<Integer, String> portToRoute) {
		this.portToRoute = portToRoute;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return portToRoute.isEmpty() || !portToRoute.containsKey(request.getLocalPort());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String route = portToRoute.get(request.getLocalPort());
		if (route == null) {
			filterChain.doFilter(request, response);
			return;
		}
		String path = pathWithinApplication(request);
		String target = "/proxy/" + route + ("/".equals(path) ? "" : path);
		request.getRequestDispatcher(target).forward(request, response);
	}

	private static String pathWithinApplication(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		String path = uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;
		if (path.isEmpty()) {
			return "/";
		}
		if (!path.startsWith("/")) {
			return "/" + path;
		}
		return path;
	}
}
