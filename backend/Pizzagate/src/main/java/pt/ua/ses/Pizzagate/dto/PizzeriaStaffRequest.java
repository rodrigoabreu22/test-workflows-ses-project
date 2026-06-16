package pt.ua.ses.Pizzagate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload used to replace the set of staff subjects for a pizzeria")
public class PizzeriaStaffRequest {

	@NotNull
	@Schema(
			description = "Subjects (Keycloak usernames) allowed to manage this pizzeria's pizzas",
			example = "[\"staff-a\"]",
			requiredMode = Schema.RequiredMode.REQUIRED
	)
	private Set<@NotBlank @Size(max = 120) String> staffSubjects;
}
