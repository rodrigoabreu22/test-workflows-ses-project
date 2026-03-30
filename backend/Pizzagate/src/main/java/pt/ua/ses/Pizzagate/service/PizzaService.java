package pt.ua.ses.Pizzagate.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.ses.Pizzagate.dto.PizzaRequest;
import pt.ua.ses.Pizzagate.exception.ResourceNotFoundException;
import pt.ua.ses.Pizzagate.model.Pizza;
import pt.ua.ses.Pizzagate.model.Pizzeria;
import pt.ua.ses.Pizzagate.repository.PizzaRepository;
import pt.ua.ses.Pizzagate.repository.PizzeriaRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PizzaService {

	private final PizzaRepository pizzaRepository;
	private final PizzeriaRepository pizzeriaRepository;

	public Pizza create(PizzaRequest request) {
		Pizzeria pizzeria = getPizzeria(request.getPizzeriaId());
		Pizza pizza = Pizza.builder()
				.name(request.getName())
				.ingredients(request.getIngredients())
				.pizzeria(pizzeria)
				.price(request.getPrice())
				.size(request.getSize())
				.isAvailable(request.getIsAvailable() == null ? Boolean.TRUE : request.getIsAvailable())
				.photoUrl(request.getPhotoUrl())
				.build();
		return pizzaRepository.save(pizza);
	}

	@Transactional(readOnly = true)
	public List<Pizza> findAll(Long pizzeriaId) {
		if (pizzeriaId == null) {
			return pizzaRepository.findAll();
		}
		return pizzaRepository.findByPizzeria_Id(pizzeriaId);
	}

	@Transactional(readOnly = true)
	public Pizza findById(Long id) {
		return getPizza(id);
	}

	public Pizza update(Long id, PizzaRequest request) {
		Pizza existing = getPizza(id);
		Pizzeria pizzeria = getPizzeria(request.getPizzeriaId());
		existing.setName(request.getName());
		existing.setIngredients(request.getIngredients());
		existing.setPizzeria(pizzeria);
		existing.setPrice(request.getPrice());
		existing.setSize(request.getSize());
		existing.setIsAvailable(request.getIsAvailable() == null ? Boolean.TRUE : request.getIsAvailable());
		existing.setPhotoUrl(request.getPhotoUrl());
		return pizzaRepository.save(existing);
	}

	public void delete(Long id) {
		Pizza existing = getPizza(id);
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
