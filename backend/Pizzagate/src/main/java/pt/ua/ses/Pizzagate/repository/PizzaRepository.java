package pt.ua.ses.Pizzagate.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.ua.ses.Pizzagate.model.Pizza;

public interface PizzaRepository extends JpaRepository<Pizza, Long> {

	List<Pizza> findByPizzeria_Id(Long pizzeriaId);
}
