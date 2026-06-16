package pt.ua.ses.Pizzagate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import pt.ua.ses.Pizzagate.model.Pizza;
import pt.ua.ses.Pizzagate.model.PizzaSize;

@Schema(description = "Pizza resource returned by the API")
public record PizzaResponse(
		@Schema(description = "Pizza identifier", example = "1")
		Long id,
		@Schema(description = "Pizza name", example = "Margherita")
		String name,
		@Schema(description = "Ingredient list or description", example = "Tomato, mozzarella, basil")
		String ingredients,
		@Schema(description = "Owning pizzeria identifier", example = "1")
		Long pizzeriaId,
		@Schema(description = "Price", example = "12.50")
		BigDecimal price,
		@Schema(description = "Pizza size", example = "medium")
		PizzaSize size,
		@Schema(description = "Whether the pizza is available", example = "true")
		Boolean isAvailable,
		@Schema(description = "Photo URL", example = "https://example.com/pizzas/margherita.jpg", format = "uri")
		String photoUrl,
		@Schema(description = "Creation timestamp", example = "2026-02-23T10:45:12", format = "date-time")
		LocalDateTime createdAt,
		@Schema(description = "Whether the current caller may update or delete this pizza")
		boolean canEdit
) {

	public static PizzaResponse from(Pizza pizza, boolean canEdit) {
		return new PizzaResponse(
				pizza.getId(),
				pizza.getName(),
				pizza.getIngredients(),
				pizza.getPizzeriaId(),
				pizza.getPrice(),
				pizza.getSize(),
				pizza.getIsAvailable(),
				pizza.getPhotoUrl(),
				pizza.getCreatedAt(),
				canEdit
		);
	}
}
