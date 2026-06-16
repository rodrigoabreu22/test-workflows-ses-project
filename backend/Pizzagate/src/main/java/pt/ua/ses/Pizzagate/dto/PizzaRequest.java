package pt.ua.ses.Pizzagate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.ua.ses.Pizzagate.model.PizzaSize;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload used to create or update a pizza")
public class PizzaRequest {

	@NotBlank
	@Size(max = 150)
	@Schema(
			description = "Pizza name",
			example = "Margherita",
			requiredMode = Schema.RequiredMode.REQUIRED
	)
	private String name;

	@Size(max = 800)
	@Schema(
			description = "Ingredient list or description",
			example = "Tomato, mozzarella, basil"
	)
	private String ingredients;

	@NotNull
	@Schema(
			description = "ID of the pizzeria that owns this pizza",
			example = "1",
			requiredMode = Schema.RequiredMode.REQUIRED
	)
	private Long pizzeriaId;

	@NotNull
	@DecimalMin("0.00")
	@Digits(integer = 3, fraction = 2)
	@Schema(
			description = "Price in euros/dollars (2 decimal places)",
			example = "12.50",
			requiredMode = Schema.RequiredMode.REQUIRED
	)
	private BigDecimal price;

	@NotNull
	@Schema(
			description = "Pizza size",
			example = "medium",
			requiredMode = Schema.RequiredMode.REQUIRED
	)
	private PizzaSize size;

	@Schema(description = "Whether the pizza is available for ordering", example = "true")
	private Boolean isAvailable;

	@Size(max = 2048)
	@Pattern(regexp = "^(https://.*)?$", message = "must be an HTTPS URL")
	@Schema(
			description = "Photo URL for display purposes",
			example = "https://example.com/pizzas/margherita.jpg",
			format = "uri"
	)
	private String photoUrl;
}
