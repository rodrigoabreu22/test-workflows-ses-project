package pt.ua.ses.Pizzagate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Writes one structured JSON line per /api/** request to the dedicated AUDIT
 * logger (stdout), captured off-host by Docker's logging driver. Registered
 * to wrap the bearer-token authentication filter so it sees both the
 * authenticated principal and any 401/403 short-circuits.
 */
@Component
public class AuditLogFilter extends OncePerRequestFilter {

	private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String path = request.getRequestURI();
		if (path == null || !path.startsWith("/api/")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			filterChain.doFilter(request, response);
		} finally {
			if (AUDIT.isInfoEnabled()) {
				logAuditEntry(request, response, path);
			}
		}
	}

	private void logAuditEntry(HttpServletRequest request, HttpServletResponse response, String path) {
		AUDIT.info(
				"{\"timestamp\":\"{}\",\"principal\":\"{}\",\"method\":\"{}\",\"path\":\"{}\","
						+ "\"resourceId\":{},\"status\":{}}",
				Instant.now(),
				currentPrincipal(),
				request.getMethod(),
				path,
				trailingNumericSegment(path),
				response.getStatus()
		);
	}

	private String currentPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return "anonymous";
		}
		return authentication.getName();
	}

	private String trailingNumericSegment(String path) {
		String lastSegment = path.substring(path.lastIndexOf('/') + 1);
		return lastSegment.matches("\\d+") ? lastSegment : "null";
	}
}
