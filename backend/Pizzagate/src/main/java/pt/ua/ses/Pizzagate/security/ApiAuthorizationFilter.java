package pt.ua.ses.Pizzagate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class ApiAuthorizationFilter extends OncePerRequestFilter {

	private final AuthorizationService authorizationService;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		RoutePermission permission = resolvePermission(request);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (permission != null && authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {
			try {
				authorizationService.assertRouteAllowed(permission.resource(), permission.action());
			} catch (AccessDeniedException ex) {
				response.sendError(HttpStatus.FORBIDDEN.value(), "Access denied.");
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	private RoutePermission resolvePermission(HttpServletRequest request) {
		String path = request.getRequestURI();
		if (path == null || !path.startsWith("/api/")) {
			return null;
		}

		String resource;
		if (path.startsWith("/api/pizzerias")) {
			resource = "pizzeria";
		} else if (path.startsWith("/api/pizzas")) {
			resource = "pizza";
		} else {
			return new RoutePermission("unknown", "unknown");
		}

		return new RoutePermission(resource, switch (request.getMethod()) {
			case "GET" -> "read";
			case "POST" -> "create";
			case "PUT", "PATCH" -> "update";
			case "DELETE" -> "delete";
			default -> "unknown";
		});
	}

	private record RoutePermission(String resource, String action) {
	}
}
