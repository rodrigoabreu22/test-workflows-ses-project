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
import org.springframework.web.bind.annotation.RestController;
import pt.ua.ses.Pizzagate.dto.ApiErrorResponse;
import pt.ua.ses.Pizzagate.dto.PizzeriaRequest;
import pt.ua.ses.Pizzagate.dto.PizzeriaResponse;
import pt.ua.ses.Pizzagate.dto.PizzeriaStaffRequest;
import pt.ua.ses.Pizzagate.service.PizzeriaService;

@RestController
@RequestMapping("/api/pizzerias")
@RequiredArgsConstructor
@Tag(name = "Pizzerias", description = "CRUD operations for pizzerias")
public class PizzeriaController {

	private final PizzeriaService pizzeriaService;

	@PostMapping
	@Operation(summary = "Create a pizzeria")
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "Pizzeria data to create",
			content = @Content(schema = @Schema(implementation = PizzeriaRequest.class))
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "201",
					description = "Pizzeria created successfully",
					content = @Content(schema = @Schema(implementation = PizzeriaResponse.class))
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
					description = "Caller does not have the PIZZERIA_OWNER role",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<PizzeriaResponse> create(@Valid @RequestBody PizzeriaRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pizzeriaService.create(request));
	}

	@GetMapping
	@Operation(summary = "List pizzerias", description = "Returns a page of pizzerias.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Pizzerias retrieved successfully",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = PizzeriaResponse.class)))
			)
	})
	public Page<PizzeriaResponse> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
		return pizzeriaService.findAll(pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a pizzeria by ID")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Pizzeria found",
					content = @Content(schema = @Schema(implementation = PizzeriaResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Pizzeria not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public PizzeriaResponse findById(
			@Parameter(description = "Pizzeria identifier", example = "1")
			@PathVariable Long id
	) {
		return pizzeriaService.findById(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a pizzeria", description = "Updates an existing pizzeria by its ID.")
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "Pizzeria data to update",
			content = @Content(schema = @Schema(implementation = PizzeriaRequest.class))
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Pizzeria updated successfully",
					content = @Content(schema = @Schema(implementation = PizzeriaResponse.class))
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
					description = "Caller is not the owner of this pizzeria",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Pizzeria not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public PizzeriaResponse update(
			@Parameter(description = "Pizzeria identifier", example = "1")
			@PathVariable Long id,
			@Valid @RequestBody PizzeriaRequest request
	) {
		return pizzeriaService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a pizzeria", description = "Deletes a pizzeria and its pizzas (cascade delete).")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Pizzeria deleted successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "No valid bearer token provided",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "403",
					description = "Caller is not the owner of this pizzeria",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Pizzeria not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> delete(
			@Parameter(description = "Pizzeria identifier", example = "1")
			@PathVariable Long id
	) {
		pizzeriaService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/staff")
	@Operation(
			summary = "Replace pizzeria staff",
			description = "Replaces the set of staff subjects allowed to manage this pizzeria's pizzas. Owner/admin only."
	)
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "Staff subjects to assign to this pizzeria",
			content = @Content(schema = @Schema(implementation = PizzeriaStaffRequest.class))
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Staff updated successfully",
					content = @Content(schema = @Schema(implementation = PizzeriaResponse.class))
			),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid request payload",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "No valid bearer token provided",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "403",
					description = "Caller is not the owner of this pizzeria",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Pizzeria not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public PizzeriaResponse updateStaff(
			@Parameter(description = "Pizzeria identifier", example = "1")
			@PathVariable Long id,
			@Valid @RequestBody PizzeriaStaffRequest request
	) {
		return pizzeriaService.updateStaff(id, request);
	}
}
