package pt.ua.ses.Pizzagate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.ua.ses.Pizzagate.dto.ApiErrorResponse;
import pt.ua.ses.Pizzagate.dto.PizzaRequest;
import pt.ua.ses.Pizzagate.dto.PizzaResponse;
import pt.ua.ses.Pizzagate.service.PizzaService;

@RestController
@RequestMapping("/api/pizzas")
@RequiredArgsConstructor
@Tag(name = "Pizzas", description = "CRUD operations for pizzas")
public class PizzaController {

	private final PizzaService pizzaService;

	@PostMapping
	@Operation(
			summary = "Create a pizza",
			description = "Creates a pizza and associates it with an existing pizzeria."
	)
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "Pizza data to create",
			content = @Content(schema = @Schema(implementation = PizzaRequest.class))
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "201",
					description = "Pizza created successfully",
					content = @Content(schema = @Schema(implementation = PizzaResponse.class))
			),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid request payload or database constraint violation",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "No valid bearer token provided",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "403",
					description = "Caller is not the owner or staff of the target pizzeria",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Referenced pizzeria not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<PizzaResponse> create(@Valid @RequestBody PizzaRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pizzaService.create(request));
	}

	@GetMapping
	@Operation(
			summary = "List pizzas",
			description = "Returns a page of pizzas. Optionally filter results by pizzeria ID."
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Pizzas retrieved successfully",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = PizzaResponse.class)))
			),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid query parameter",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public Page<PizzaResponse> findAll(
			@Parameter(description = "Filter pizzas by pizzeria ID", example = "1")
			@RequestParam(required = false) Long pizzeriaId,
			@PageableDefault(size = 20, sort = "id") Pageable pageable
	) {
		return pizzaService.findAll(pizzeriaId, pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a pizza by ID")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Pizza found",
					content = @Content(schema = @Schema(implementation = PizzaResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Pizza not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public PizzaResponse findById(
			@Parameter(description = "Pizza identifier", example = "1")
			@PathVariable Long id
	) {
		return pizzaService.findById(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a pizza", description = "Updates an existing pizza by its ID.")
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "Pizza data to update",
			content = @Content(schema = @Schema(implementation = PizzaRequest.class))
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Pizza updated successfully",
					content = @Content(schema = @Schema(implementation = PizzaResponse.class))
			),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid request payload or database constraint violation",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "No valid bearer token provided",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "403",
					description = "Caller is not the owner or staff of the associated pizzeria",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Pizza or referenced pizzeria not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public PizzaResponse update(
			@Parameter(description = "Pizza identifier", example = "1")
			@PathVariable Long id,
			@Valid @RequestBody PizzaRequest request
	) {
		return pizzaService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a pizza", description = "Deletes a pizza by its ID.")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Pizza deleted successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "No valid bearer token provided",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "403",
					description = "Caller is not the owner or staff of the associated pizzeria",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Pizza not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> delete(
			@Parameter(description = "Pizza identifier", example = "1")
			@PathVariable Long id
	) {
		pizzaService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
