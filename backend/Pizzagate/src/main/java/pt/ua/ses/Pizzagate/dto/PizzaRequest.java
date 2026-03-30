package pt.ua.ses.Pizzagate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PizzaRequest {

	@NotBlank
	@Size(max = 150)
	private String name;

	@Size(max = 800)
	private String ingredients;

	@NotNull
	private Long pizzeriaId;

	@NotNull
	@DecimalMin("0.00")
	@Digits(integer = 3, fraction = 2)
	private BigDecimal price;

	@NotNull
	private PizzaSize size;

	private Boolean isAvailable;

	private String photoUrl;
}
