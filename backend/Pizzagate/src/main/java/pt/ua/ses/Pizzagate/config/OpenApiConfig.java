package pt.ua.ses.Pizzagate.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "Pizzagate API",
				version = "2.0.0",
				description = """
						REST API for the Pizzagate pizzeria-catalogue platform.

						**Authentication:** write operations (POST, PUT, DELETE) require a bearer JWT \
						issued by the Keycloak identity provider. \
						GET endpoints are public and do not require a token.

						**Authorisation:** object-level ownership is enforced server-side. \
						A PIZZERIA_OWNER may only mutate their own pizzeria and its pizzas; \
						PIZZERIA_STAFF may only mutate pizzas of their assigned pizzeria; \
						ADMIN is unrestricted."""
		)
)
@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT",
		in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
