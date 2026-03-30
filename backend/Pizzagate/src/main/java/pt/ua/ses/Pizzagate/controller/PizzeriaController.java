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
import org.springframework.web.bind.annotation.RestController;
import pt.ua.ses.Pizzagate.dto.PizzeriaRequest;
import pt.ua.ses.Pizzagate.model.Pizzeria;
import pt.ua.ses.Pizzagate.service.PizzeriaService;

@RestController
@RequestMapping("/api/pizzerias")
@RequiredArgsConstructor
public class PizzeriaController {

	private final PizzeriaService pizzeriaService;

	@PostMapping
	public ResponseEntity<Pizzeria> create(@Valid @RequestBody PizzeriaRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pizzeriaService.create(request));
	}

	@GetMapping
	public List<Pizzeria> findAll() {
		return pizzeriaService.findAll();
	}

	@GetMapping("/{id}")
	public Pizzeria findById(@PathVariable Long id) {
		return pizzeriaService.findById(id);
	}

	@PutMapping("/{id}")
	public Pizzeria update(@PathVariable Long id, @Valid @RequestBody PizzeriaRequest request) {
		return pizzeriaService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		pizzeriaService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
