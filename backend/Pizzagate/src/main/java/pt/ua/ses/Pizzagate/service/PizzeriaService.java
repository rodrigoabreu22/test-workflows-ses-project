package pt.ua.ses.Pizzagate.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.ses.Pizzagate.dto.PizzeriaRequest;
import pt.ua.ses.Pizzagate.exception.ResourceNotFoundException;
import pt.ua.ses.Pizzagate.model.Pizzeria;
import pt.ua.ses.Pizzagate.repository.PizzeriaRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PizzeriaService {

	private final PizzeriaRepository pizzeriaRepository;

	public Pizzeria create(PizzeriaRequest request) {
		Pizzeria pizzeria = Pizzeria.builder()
				.name(request.getName())
				.address(request.getAddress())
				.phone(request.getPhone())
				.build();
		return pizzeriaRepository.save(pizzeria);
	}

	@Transactional(readOnly = true)
	public List<Pizzeria> findAll() {
		return pizzeriaRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Pizzeria findById(Long id) {
		return getExisting(id);
	}

	public Pizzeria update(Long id, PizzeriaRequest request) {
		Pizzeria existing = getExisting(id);
		existing.setName(request.getName());
		existing.setAddress(request.getAddress());
		existing.setPhone(request.getPhone());
		return pizzeriaRepository.save(existing);
	}

	public void delete(Long id) {
		Pizzeria existing = getExisting(id);
		pizzeriaRepository.delete(existing);
	}

	private Pizzeria getExisting(Long id) {
		return pizzeriaRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Pizzeria with id " + id + " was not found."));
	}
}
