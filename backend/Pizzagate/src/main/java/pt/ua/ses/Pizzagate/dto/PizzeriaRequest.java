package pt.ua.ses.Pizzagate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Payload used to create or update a pizzeria")
public class PizzeriaRequest {

	@NotBlank
	@Size(max = 150)
	@Schema(
			description = "Pizzeria name",
			example = "Napoli Centrale",
			requiredMode = Schema.RequiredMode.REQUIRED
	)
	private String name;

	@Size(max = 255)
	@Schema(description = "Street address", example = "123 Main St, Aveiro")
	private String address;

	@Size(max = 30)
	@Schema(description = "Contact phone number", example = "+351912345678")
	private String phone;
}
