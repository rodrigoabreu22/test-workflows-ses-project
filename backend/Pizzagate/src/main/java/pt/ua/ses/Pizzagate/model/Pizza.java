package pt.ua.ses.Pizzagate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.hibernate.annotations.CreationTimestamp;

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
@Schema(description = "Pizza resource returned by the API")
public class Pizza {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "Pizza identifier", example = "1")
	private Long id;

	@Column(nullable = false, length = 150)
	@Schema(description = "Pizza name", example = "Margherita")
	private String name;

	@Column(length = 800)
	@Schema(description = "Ingredient list or description", example = "Tomato, mozzarella, basil")
	private String ingredients;

	@ManyToOne(optional = false)
	@JoinColumn(
			name = "pizzeria_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_pizzas_pizzerias")
	)
	@JsonIgnore
	@Schema(hidden = true)
	private Pizzeria pizzeria;

	@Column(nullable = false, precision = 5, scale = 2)
	@Schema(description = "Price", example = "12.50")
	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Schema(description = "Pizza size", example = "medium")
	private PizzaSize size;

	@Column(name = "is_available", nullable = false)
	@Schema(description = "Whether the pizza is available", example = "true")
	private Boolean isAvailable;

	@Lob
	@Column(name = "photo_url")
	@Schema(description = "Photo URL", example = "https://example.com/pizzas/margherita.jpg", format = "uri")
	private String photoUrl;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	@Schema(description = "Creation timestamp", example = "2026-02-23T10:45:12", format = "date-time")
	private LocalDateTime createdAt;

	@JsonProperty("pizzeriaId")
	@Schema(description = "Owning pizzeria identifier", example = "1")
	public Long getPizzeriaId() {
		return pizzeria != null ? pizzeria.getId() : null;
	}
}
