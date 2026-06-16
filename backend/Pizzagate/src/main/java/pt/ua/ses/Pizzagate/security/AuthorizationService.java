package pt.ua.ses.Pizzagate.security;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

	private final Map<PermissionKey, List<PermissionRule>> permissions = new HashMap<>();

	@PostConstruct
	void loadPermissions() throws IOException {
		ClassPathResource resource = new ClassPathResource("security/permissions.csv");
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			reader.lines()
					.skip(1)
					.map(String::trim)
					.filter(line -> !line.isEmpty() && !line.startsWith("#"))
					.map(this::parseRule)
					.forEach(rule -> permissions
							.computeIfAbsent(new PermissionKey(rule.resource(), rule.action()), ignored -> new ArrayList<>())
							.add(rule));
		}
	}

	public AuthenticatedUser currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AccessDeniedException("Authentication is required.");
		}

		String subject = authentication.getName();
		Set<String> roles = authentication.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.filter(authority -> authority.startsWith("ROLE_"))
				.map(authority -> authority.substring("ROLE_".length()))
				.collect(Collectors.toCollection(HashSet::new));

		return new AuthenticatedUser(subject, roles);
	}

	public void assertRouteAllowed(String resource, String action) {
		AuthenticatedUser user = currentUser();
		boolean allowed = getRules(resource, action)
				.stream()
				.anyMatch(rule -> user.roles().contains(rule.role()));
		if (!allowed) {
			throw new AccessDeniedException("Role is not allowed to perform this action.");
		}
	}

	public void assertObjectAllowed(String resource, String action, String ownerSubject) {
		assertObjectAllowed(resource, action, Set.of(ownerSubject));
	}

	public void assertObjectAllowed(String resource, String action, Set<String> ownerSubjects) {
		if (!canAccess(resource, action, ownerSubjects)) {
			throw new AccessDeniedException("User is not allowed to access this resource.");
		}
	}

	public boolean canAccess(String resource, String action, String ownerSubject) {
		return canAccess(resource, action, Set.of(ownerSubject));
	}

	public boolean canAccess(String resource, String action, Set<String> ownerSubjects) {
		AuthenticatedUser user = currentUser();
		return getRules(resource, action)
				.stream()
				.anyMatch(rule -> user.roles().contains(rule.role())
						&& (!rule.ownershipRequired() || ownerSubjects.contains(user.subject())));
	}

	public boolean owns(String ownerSubject) {
		return currentUser().subject().equals(ownerSubject);
	}

	public Set<String> currentRoles() {
		return currentUser().roles();
	}

	private List<PermissionRule> getRules(String resource, String action) {
		return permissions.getOrDefault(
				new PermissionKey(normalize(resource), normalize(action)),
				List.of()
		);
	}

	private PermissionRule parseRule(String line) {
		String[] columns = line.split(",");
		if (columns.length != 4) {
			throw new IllegalStateException("Invalid permission row: " + line);
		}
		return new PermissionRule(
				normalize(columns[0]),
				normalize(columns[1]),
				columns[2].trim().toUpperCase(Locale.ROOT),
				Boolean.parseBoolean(columns[3].trim())
		);
	}

	private String normalize(String value) {
		return value.trim().toLowerCase(Locale.ROOT);
	}

	private record PermissionKey(String resource, String action) {
	}

	private record PermissionRule(String resource, String action, String role, boolean ownershipRequired) {
	}
}
