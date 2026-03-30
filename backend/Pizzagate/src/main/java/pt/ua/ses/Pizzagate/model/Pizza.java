package pt.ua.ses.Pizzagate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "pizzas",
		indexes = {
				@Index(name = "idx_pizzas_pizzeria_id", columnList = "pizzeria_id"),
				@Index(name = "idx_pizzas_name", columnList = "name")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pizza {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(length = 800)
	private String ingredients;

	@ManyToOne(optional = false)
	@JoinColumn(
			name = "pizzeria_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_pizzas_pizzerias")
	)
	@JsonIgnore
	private Pizzeria pizzeria;

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PizzaSize size;

	@Column(name = "is_available", nullable = false)
	private Boolean isAvailable;

	@Lob
	@Column(name = "photo_url")
	private String photoUrl;

	@Column(name = "created_at", nullable = false, updatable = false, insertable = false)
	private LocalDateTime createdAt;

	@JsonProperty("pizzeriaId")
	public Long getPizzeriaId() {
		return pizzeria != null ? pizzeria.getId() : null;
	}
}
