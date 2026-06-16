package pt.ua.ses.Pizzagate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
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
@Schema(name = "ApiErrorResponse", description = "Standard error response returned by the API")
public class ApiErrorResponse {

	@Schema(description = "Time when the error was generated", example = "2026-02-23T10:45:12.345")
	private LocalDateTime timestamp;

	@Schema(description = "HTTP status code", example = "400")
	private Integer status;

	@Schema(description = "HTTP status reason phrase", example = "Bad Request")
	private String error;

	@Schema(
			description = "Application error message or validation details",
			example = "name: must not be blank"
	)
	private String message;
}
