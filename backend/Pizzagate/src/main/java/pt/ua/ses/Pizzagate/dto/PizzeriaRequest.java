package pt.ua.ses.Pizzagate.dto;

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
public class PizzeriaRequest {

	@NotBlank
	@Size(max = 150)
	private String name;

	@Size(max = 255)
	private String address;

	@Size(max = 30)
	private String phone;
}
