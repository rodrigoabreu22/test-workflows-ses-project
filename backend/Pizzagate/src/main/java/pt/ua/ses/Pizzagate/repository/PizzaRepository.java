package pt.ua.ses.Pizzagate.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.ua.ses.Pizzagate.model.Pizza;

public interface PizzaRepository extends JpaRepository<Pizza, Long> {

	Page<Pizza> findByPizzeria_Id(Long pizzeriaId, Pageable pageable);

	List<Pizza> findByPizzeria_OwnerSubject(String ownerSubject);

	List<Pizza> findByPizzeria_IdAndPizzeria_OwnerSubject(Long pizzeriaId, String ownerSubject);
}
