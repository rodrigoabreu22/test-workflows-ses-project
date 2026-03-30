package pt.ua.ses.Pizzagate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ua.ses.Pizzagate.model.Pizzeria;

public interface PizzeriaRepository extends JpaRepository<Pizzeria, Long> {
}
