package pt.ua.ses.Pizzagate.service;

import java.util.HashSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.ses.Pizzagate.dto.PizzeriaRequest;
import pt.ua.ses.Pizzagate.dto.PizzeriaResponse;
import pt.ua.ses.Pizzagate.dto.PizzeriaStaffRequest;
import pt.ua.ses.Pizzagate.exception.ResourceNotFoundException;
import pt.ua.ses.Pizzagate.model.Pizzeria;
import pt.ua.ses.Pizzagate.repository.PizzeriaRepository;
import pt.ua.ses.Pizzagate.security.AuthenticatedUser;
import pt.ua.ses.Pizzagate.security.AuthorizationService;

@Service
@RequiredArgsConstructor
@Transactional
public class PizzeriaService {

	private final PizzeriaRepository pizzeriaRepository;
	private final AuthorizationService authorizationService;

	public PizzeriaResponse create(PizzeriaRequest request) {
		AuthenticatedUser user = authorizationService.currentUser();
		Pizzeria pizzeria = Pizzeria.builder()
				.name(request.getName())
				.address(request.getAddress())
				.phone(request.getPhone())
				.ownerSubject(user.subject())
				.build();
		return PizzeriaResponse.from(pizzeriaRepository.save(pizzeria), true);
	}

	@Transactional(readOnly = true)
	public Page<PizzeriaResponse> findAll(Pageable pageable) {
		return pizzeriaRepository.findAll(pageable)
				.map(p -> PizzeriaResponse.from(p, authorizationService.canAccess("pizzeria", "update", p.getOwnerSubject())));
	}

	@Transactional(readOnly = true)
	public PizzeriaResponse findById(Long id) {
		Pizzeria pizzeria = getExisting(id);
		authorizationService.assertObjectAllowed("pizzeria", "read", pizzeria.getOwnerSubject());
		return PizzeriaResponse.from(pizzeria, authorizationService.canAccess("pizzeria", "update", pizzeria.getOwnerSubject()));
	}

	public PizzeriaResponse update(Long id, PizzeriaRequest request) {
		Pizzeria existing = getExisting(id);
		authorizationService.assertObjectAllowed("pizzeria", "update", existing.getOwnerSubject());
		existing.setName(request.getName());
		existing.setAddress(request.getAddress());
		existing.setPhone(request.getPhone());
		return PizzeriaResponse.from(pizzeriaRepository.save(existing), true);
	}

	public void delete(Long id) {
		Pizzeria existing = getExisting(id);
		authorizationService.assertObjectAllowed("pizzeria", "delete", existing.getOwnerSubject());
		pizzeriaRepository.delete(existing);
	}

	public PizzeriaResponse updateStaff(Long id, PizzeriaStaffRequest request) {
		Pizzeria existing = getExisting(id);
		authorizationService.assertObjectAllowed("pizzeria", "update", existing.getOwnerSubject());
		existing.setStaffSubjects(new HashSet<>(request.getStaffSubjects()));
		return PizzeriaResponse.from(pizzeriaRepository.save(existing), true);
	}

	private Pizzeria getExisting(Long id) {
		return pizzeriaRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Pizzeria with id " + id + " was not found."));
	}
}
