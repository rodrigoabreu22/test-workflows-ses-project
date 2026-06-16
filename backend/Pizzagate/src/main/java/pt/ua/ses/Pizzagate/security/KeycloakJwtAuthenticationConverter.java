package pt.ua.ses.Pizzagate.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		String principalName = jwt.getClaimAsString("preferred_username");
		if (principalName == null || principalName.isBlank()) {
			principalName = jwt.getSubject();
		}
		return new JwtAuthenticationToken(jwt, extractAuthorities(jwt), principalName);
	}

	private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		Set<String> roles = new HashSet<>();
		Object realmAccess = jwt.getClaim("realm_access");
		if (realmAccess instanceof Map<?, ?> realmAccessMap) {
			Object rawRoles = realmAccessMap.get("roles");
			if (rawRoles instanceof Collection<?> roleCollection) {
				roleCollection.stream()
						.map(String::valueOf)
						.forEach(roles::add);
			}
		}

		List<String> scopes = jwt.getClaimAsStringList("scope");
		if (scopes != null) {
			roles.addAll(scopes);
		}

		return roles.stream()
				.map(role -> role.toUpperCase(Locale.ROOT).replace('-', '_'))
				.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toSet());
	}
}
