package pt.ua.ses.Pizzagate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
		name = "pizzerias",
		indexes = {
				@Index(name = "idx_pizzerias_name", columnList = "name"),
				@Index(name = "idx_pizzerias_owner_subject", columnList = "owner_subject")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Pizzeria resource returned by the API")
public class Pizzeria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "Pizzeria identifier", example = "1")
	private Long id;

	@Column(nullable = false, length = 150)
	@Schema(description = "Pizzeria name", example = "Napoli Centrale")
	private String name;

	@Column(length = 255)
	@Schema(description = "Street address", example = "123 Main St, Aveiro")
	private String address;

	@Column(length = 30)
	@Schema(description = "Contact phone number", example = "+351912345678")
	private String phone;

	@JsonIgnore
	@Column(name = "owner_subject", nullable = false, length = 120)
	@Schema(hidden = true)
	private String ownerSubject;

	@JsonIgnore
	@ElementCollection
	@CollectionTable(name = "pizzeria_staff", joinColumns = @JoinColumn(name = "pizzeria_id"))
	@Column(name = "staff_subject")
	@Builder.Default
	@Schema(hidden = true)
	private Set<String> staffSubjects = new HashSet<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	@Schema(description = "Creation timestamp", example = "2026-02-23T10:45:12", format = "date-time")
	private LocalDateTime createdAt;

	/**
	 * Subjects allowed to manage this pizzeria's pizzas: its owner plus any assigned staff.
	 */
	public Set<String> pizzaManagers() {
		Set<String> managers = new HashSet<>(staffSubjects);
		managers.add(ownerSubject);
		return managers;
	}
}
