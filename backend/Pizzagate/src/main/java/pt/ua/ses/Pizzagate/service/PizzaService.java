package pt.ua.ses.Pizzagate.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.ses.Pizzagate.dto.PizzaRequest;
import pt.ua.ses.Pizzagate.dto.PizzaResponse;
import pt.ua.ses.Pizzagate.exception.ResourceNotFoundException;
import pt.ua.ses.Pizzagate.model.Pizza;
import pt.ua.ses.Pizzagate.model.Pizzeria;
import pt.ua.ses.Pizzagate.repository.PizzaRepository;
import pt.ua.ses.Pizzagate.repository.PizzeriaRepository;
import pt.ua.ses.Pizzagate.security.AuthorizationService;

@Service
@RequiredArgsConstructor
@Transactional
public class PizzaService {

	private final PizzaRepository pizzaRepository;
	private final PizzeriaRepository pizzeriaRepository;
	private final AuthorizationService authorizationService;

	public PizzaResponse create(PizzaRequest request) {
		Pizzeria pizzeria = getPizzeria(request.getPizzeriaId());
		authorizationService.assertObjectAllowed("pizza", "create", pizzeria.pizzaManagers());
		Pizza pizza = Pizza.builder()
				.name(request.getName())
				.ingredients(request.getIngredients())
				.pizzeria(pizzeria)
				.price(request.getPrice())
				.size(request.getSize())
				.isAvailable(request.getIsAvailable() == null ? Boolean.TRUE : request.getIsAvailable())
				.photoUrl(request.getPhotoUrl())
				.build();
		return PizzaResponse.from(pizzaRepository.save(pizza), true);
	}

	@Transactional(readOnly = true)
	public Page<PizzaResponse> findAll(Long pizzeriaId, Pageable pageable) {
		Page<Pizza> pizzas = pizzeriaId == null
				? pizzaRepository.findAll(pageable)
				: pizzaRepository.findByPizzeria_Id(pizzeriaId, pageable);
		return pizzas.map(p -> PizzaResponse.from(p, authorizationService.canAccess("pizza", "update", p.getPizzeria().pizzaManagers())));
	}

	@Transactional(readOnly = true)
	public PizzaResponse findById(Long id) {
		Pizza pizza = getPizza(id);
		authorizationService.assertObjectAllowed("pizza", "read", pizza.getPizzeria().pizzaManagers());
		return PizzaResponse.from(pizza, authorizationService.canAccess("pizza", "update", pizza.getPizzeria().pizzaManagers()));
	}

	public PizzaResponse update(Long id, PizzaRequest request) {
		Pizza existing = getPizza(id);
		authorizationService.assertObjectAllowed("pizza", "update", existing.getPizzeria().pizzaManagers());
		Pizzeria pizzeria = getPizzeria(request.getPizzeriaId());
		authorizationService.assertObjectAllowed("pizza", "create", pizzeria.pizzaManagers());
		existing.setName(request.getName());
		existing.setIngredients(request.getIngredients());
		existing.setPizzeria(pizzeria);
		existing.setPrice(request.getPrice());
		existing.setSize(request.getSize());
		existing.setIsAvailable(request.getIsAvailable() == null ? Boolean.TRUE : request.getIsAvailable());
		existing.setPhotoUrl(request.getPhotoUrl());
		return PizzaResponse.from(pizzaRepository.save(existing), true);
	}

	public void delete(Long id) {
		Pizza existing = getPizza(id);
		authorizationService.assertObjectAllowed("pizza", "delete", existing.getPizzeria().pizzaManagers());
		pizzaRepository.delete(existing);
	}

	private Pizza getPizza(Long id) {
		return pizzaRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Pizza with id " + id + " was not found."));
	}

	private Pizzeria getPizzeria(Long id) {
		return pizzeriaRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Pizzeria with id " + id + " was not found."));
	}
}
