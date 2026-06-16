package pt.ua.ses.Pizzagate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import pt.ua.ses.Pizzagate.model.Pizza;
import pt.ua.ses.Pizzagate.model.PizzaSize;
import pt.ua.ses.Pizzagate.model.Pizzeria;
import pt.ua.ses.Pizzagate.repository.PizzaRepository;
import pt.ua.ses.Pizzagate.repository.PizzeriaRepository;

@SpringBootTest
@AutoConfigureMockMvc
class StaffAuthorizationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PizzeriaRepository pizzeriaRepository;

	@Autowired
	private PizzaRepository pizzaRepository;

	private Pizzeria napoliCentro;
	private Pizzeria romaAntica;
	private Pizza napoliPizza;
	private Pizza romaPizza;

	@BeforeEach
	void setUp() {
		pizzaRepository.deleteAll();
		pizzeriaRepository.deleteAll();

		napoliCentro = pizzeriaRepository.save(Pizzeria.builder()
				.name("Napoli Centro")
				.address("Rua das Flores 10")
				.phone("+351 220 100 100")
				.ownerSubject("owner-a")
				.staffSubjects(new java.util.HashSet<>(Set.of("staff-a")))
				.build());
		romaAntica = pizzeriaRepository.save(Pizzeria.builder()
				.name("Roma Antica")
				.address("Avenida da Liberdade 22")
				.phone("+351 210 200 200")
				.ownerSubject("owner-b")
				.build());
		napoliPizza = pizzaRepository.save(Pizza.builder()
				.name("Margherita")
				.ingredients("Tomato, mozzarella, basil")
				.pizzeria(napoliCentro)
				.price(new BigDecimal("8.90"))
				.size(PizzaSize.medium)
				.isAvailable(true)
				.build());
		romaPizza = pizzaRepository.save(Pizza.builder()
				.name("Quattro Formaggi")
				.ingredients("Mozzarella, gorgonzola, parmesan, fontina")
				.pizzeria(romaAntica)
				.price(new BigDecimal("9.90"))
				.size(PizzaSize.medium)
				.isAvailable(true)
				.build());
	}

	@Test
	void staffCanCreatePizzaForAssignedPizzeria() throws Exception {
		mockMvc.perform(post("/api/pizzas")
						.with(userJwt("staff-a", "PIZZERIA_STAFF"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Diavola","pizzeriaId":%d,"price":9.50,"size":"medium"}
								""".formatted(napoliCentro.getId())))
				.andExpect(status().isCreated());
	}

	@Test
	void staffCanUpdateAndDeletePizzaForAssignedPizzeria() throws Exception {
		mockMvc.perform(put("/api/pizzas/{id}", napoliPizza.getId())
						.with(userJwt("staff-a", "PIZZERIA_STAFF"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Margherita Updated","pizzeriaId":%d,"price":8.90,"size":"medium"}
								""".formatted(napoliCentro.getId())))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/api/pizzas/{id}", napoliPizza.getId())
						.with(userJwt("staff-a", "PIZZERIA_STAFF")))
				.andExpect(status().isNoContent());
	}

	@Test
	void staffCannotCreatePizzaForUnassignedPizzeria() throws Exception {
		mockMvc.perform(post("/api/pizzas")
						.with(userJwt("staff-a", "PIZZERIA_STAFF"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Diavola","pizzeriaId":%d,"price":9.50,"size":"medium"}
								""".formatted(romaAntica.getId())))
				.andExpect(status().isForbidden());
	}

	@Test
	void staffCannotUpdateOrDeletePizzaForUnassignedPizzeria() throws Exception {
		mockMvc.perform(put("/api/pizzas/{id}", romaPizza.getId())
						.with(userJwt("staff-a", "PIZZERIA_STAFF"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Hacked","pizzeriaId":%d,"price":1.00,"size":"small"}
								""".formatted(romaAntica.getId())))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/api/pizzas/{id}", romaPizza.getId())
						.with(userJwt("staff-a", "PIZZERIA_STAFF")))
				.andExpect(status().isForbidden());
	}

	@Test
	void staffCannotUpdateOrDeleteAssignedPizzeria() throws Exception {
		mockMvc.perform(put("/api/pizzerias/{id}", napoliCentro.getId())
						.with(userJwt("staff-a", "PIZZERIA_STAFF"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Hacked Name","address":"Rua Nova","phone":"+351 222 222 222"}
								"""))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/api/pizzerias/{id}", napoliCentro.getId())
						.with(userJwt("staff-a", "PIZZERIA_STAFF")))
				.andExpect(status().isForbidden());
	}

	@Test
	void staffCannotManageStaffList() throws Exception {
		mockMvc.perform(put("/api/pizzerias/{id}/staff", napoliCentro.getId())
						.with(userJwt("staff-a", "PIZZERIA_STAFF"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"staffSubjects":["staff-a","staff-b"]}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void ownerCanManageStaffList() throws Exception {
		mockMvc.perform(put("/api/pizzerias/{id}/staff", napoliCentro.getId())
						.with(userJwt("owner-a", "PIZZERIA_OWNER"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"staffSubjects":["staff-a","staff-b"]}
								"""))
				.andExpect(status().isOk());
	}

	private static org.springframework.test.web.servlet.request.RequestPostProcessor userJwt(
			String username,
			String role
	) {
		return jwt()
				.jwt(token -> token
						.subject(username)
						.claim("preferred_username", username))
				.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
