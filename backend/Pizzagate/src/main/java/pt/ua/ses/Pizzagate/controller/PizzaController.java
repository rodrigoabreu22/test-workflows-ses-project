package pt.ua.ses.Pizzagate.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import pt.ua.ses.Pizzagate.dto.PizzaRequest;
import pt.ua.ses.Pizzagate.model.Pizza;
import pt.ua.ses.Pizzagate.service.PizzaService;

@RestController
@RequestMapping("/api/pizzas")
@RequiredArgsConstructor
public class PizzaController {

	private final PizzaService pizzaService;

	@PostMapping
	public ResponseEntity<Pizza> create(@Valid @RequestBody PizzaRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pizzaService.create(request));
	}

	@GetMapping
	public List<Pizza> findAll(@RequestParam(required = false) Long pizzeriaId) {
		return pizzaService.findAll(pizzeriaId);
	}

	@GetMapping("/{id}")
	public Pizza findById(@PathVariable Long id) {
		return pizzaService.findById(id);
	}

	@PutMapping("/{id}")
	public Pizza update(@PathVariable Long id, @Valid @RequestBody PizzaRequest request) {
		return pizzaService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		pizzaService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
