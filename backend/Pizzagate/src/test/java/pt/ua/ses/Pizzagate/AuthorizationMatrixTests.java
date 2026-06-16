package pt.ua.ses.Pizzagate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import pt.ua.ses.Pizzagate.security.AuthorizationService;

@SpringBootTest
class AuthorizationMatrixTests {

	@Autowired
	private AuthorizationService authorizationService;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@ParameterizedTest
	@CsvSource({
			"CUSTOMER,pizzeria,read,true",
			"CUSTOMER,pizzeria,create,false",
			"CUSTOMER,pizzeria,update,false",
			"CUSTOMER,pizzeria,delete,false",
			"CUSTOMER,pizza,read,true",
			"CUSTOMER,pizza,create,false",
			"CUSTOMER,pizza,update,false",
			"CUSTOMER,pizza,delete,false",
			"PIZZERIA_OWNER,pizzeria,read,true",
			"PIZZERIA_OWNER,pizzeria,create,true",
			"PIZZERIA_OWNER,pizzeria,update,true",
			"PIZZERIA_OWNER,pizzeria,delete,true",
			"PIZZERIA_OWNER,pizza,read,true",
			"PIZZERIA_OWNER,pizza,create,true",
			"PIZZERIA_OWNER,pizza,update,true",
			"PIZZERIA_OWNER,pizza,delete,true",
			"PIZZERIA_STAFF,pizzeria,read,true",
			"PIZZERIA_STAFF,pizzeria,create,false",
			"PIZZERIA_STAFF,pizzeria,update,false",
			"PIZZERIA_STAFF,pizzeria,delete,false",
			"PIZZERIA_STAFF,pizza,read,true",
			"PIZZERIA_STAFF,pizza,create,true",
			"PIZZERIA_STAFF,pizza,update,true",
			"PIZZERIA_STAFF,pizza,delete,true",
			"ADMIN,pizzeria,read,true",
			"ADMIN,pizzeria,create,true",
			"ADMIN,pizzeria,update,true",
			"ADMIN,pizzeria,delete,true",
			"ADMIN,pizza,read,true",
			"ADMIN,pizza,create,true",
			"ADMIN,pizza,update,true",
			"ADMIN,pizza,delete,true"
	})
	void routeLevelPermissionMatrix(String role, String resource, String action, boolean expectedAllowed) {
		authenticate("subject", role);

		if (expectedAllowed) {
			assertThatCode(() -> authorizationService.assertRouteAllowed(resource, action))
					.doesNotThrowAnyException();
		} else {
			assertThatThrownBy(() -> authorizationService.assertRouteAllowed(resource, action))
					.isInstanceOf(AccessDeniedException.class);
		}
	}

	@ParameterizedTest
	@CsvSource({
			"CUSTOMER,owner-a,pizzeria,update,owner-a,false",
			"PIZZERIA_OWNER,owner-a,pizzeria,update,owner-a,true",
			"PIZZERIA_OWNER,owner-a,pizzeria,update,owner-b,false",
			"PIZZERIA_OWNER,owner-a,pizza,delete,owner-a,true",
			"PIZZERIA_OWNER,owner-a,pizza,delete,owner-b,false",
			"ADMIN,admin,pizzeria,delete,owner-b,true",
			"ADMIN,admin,pizza,update,owner-b,true",
			"PIZZERIA_STAFF,staff-a,pizza,update,owner-a,false",
			"PIZZERIA_STAFF,staff-a,pizzeria,update,staff-a,false"
	})
	void objectLevelPermissionMatrix(
			String role,
			String subject,
			String resource,
			String action,
			String ownerSubject,
			boolean expectedAllowed
	) {
		authenticate(subject, role);

		if (expectedAllowed) {
			assertThatCode(() -> authorizationService.assertObjectAllowed(resource, action, ownerSubject))
					.doesNotThrowAnyException();
		} else {
			assertThatThrownBy(() -> authorizationService.assertObjectAllowed(resource, action, ownerSubject))
					.isInstanceOf(AccessDeniedException.class);
		}
	}

	@ParameterizedTest
	@CsvSource({
			"staff-a,pizza,update,true",
			"staff-b,pizza,update,false"
	})
	void objectLevelPermissionMatrixWithOwnerSubjectSet(
			String subject,
			String resource,
			String action,
			boolean expectedAllowed
	) {
		authenticate(subject, "PIZZERIA_STAFF");
		Set<String> pizzaManagers = Set.of("owner-a", "staff-a");

		if (expectedAllowed) {
			assertThatCode(() -> authorizationService.assertObjectAllowed(resource, action, pizzaManagers))
					.doesNotThrowAnyException();
		} else {
			assertThatThrownBy(() -> authorizationService.assertObjectAllowed(resource, action, pizzaManagers))
					.isInstanceOf(AccessDeniedException.class);
		}
	}

	@Test
	void staffCannotUpdatePizzeriaEvenWhenInOwnerSubjectSet() {
		authenticate("staff-a", "PIZZERIA_STAFF");
		Set<String> pizzaManagers = Set.of("owner-a", "staff-a");

		assertThatThrownBy(() -> authorizationService.assertObjectAllowed("pizzeria", "update", pizzaManagers))
				.isInstanceOf(AccessDeniedException.class);
	}

	private void authenticate(String subject, String role) {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
				subject,
				"n/a",
				List.of(new SimpleGrantedAuthority("ROLE_" + role))
		));
	}
}
