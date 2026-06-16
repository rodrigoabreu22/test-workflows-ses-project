package pt.ua.ses.Pizzagate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import pt.ua.ses.Pizzagate.model.Pizzeria;

@Schema(description = "Pizzeria resource returned by the API")
public record PizzeriaResponse(
		@Schema(description = "Pizzeria identifier", example = "1")
		Long id,
		@Schema(description = "Pizzeria name", example = "Napoli Centrale")
		String name,
		@Schema(description = "Street address", example = "123 Main St, Aveiro")
		String address,
		@Schema(description = "Contact phone number", example = "+351912345678")
		String phone,
		@Schema(description = "Creation timestamp", example = "2026-02-23T10:45:12", format = "date-time")
		LocalDateTime createdAt,
		@Schema(description = "Whether the current caller may update or delete this pizzeria")
		boolean canEdit,
		@Schema(description = "Subjects allowed to manage this pizzeria's pizzas (only visible to the owner/admin)")
		Set<String> staffSubjects
) {

	public static PizzeriaResponse from(Pizzeria pizzeria, boolean canEdit) {
		return new PizzeriaResponse(
				pizzeria.getId(),
				pizzeria.getName(),
				pizzeria.getAddress(),
				pizzeria.getPhone(),
				pizzeria.getCreatedAt(),
				canEdit,
				canEdit ? pizzeria.getStaffSubjects() : null
		);
	}
}
