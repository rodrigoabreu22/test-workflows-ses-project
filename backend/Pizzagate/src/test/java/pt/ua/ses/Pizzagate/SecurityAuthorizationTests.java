package pt.ua.ses.Pizzagate;

import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class SecurityAuthorizationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PizzeriaRepository pizzeriaRepository;

	@Autowired
	private PizzaRepository pizzaRepository;

	private Pizzeria ownerAPizzeria;
	private Pizzeria ownerBPizzeria;

	@BeforeEach
	void setUp() {
		pizzaRepository.deleteAll();
		pizzeriaRepository.deleteAll();

		ownerAPizzeria = pizzeriaRepository.save(Pizzeria.builder()
				.name("Napoli Centro")
				.address("Rua das Flores 10")
				.phone("+351 220 100 100")
				.ownerSubject("owner-a")
				.build());
		ownerBPizzeria = pizzeriaRepository.save(Pizzeria.builder()
				.name("Roma Antica")
				.address("Avenida da Liberdade 22")
				.phone("+351 210 200 200")
				.ownerSubject("owner-b")
				.build());
		pizzaRepository.save(Pizza.builder()
				.name("Margherita")
				.ingredients("Tomato, mozzarella, basil")
				.pizzeria(ownerAPizzeria)
				.price(new BigDecimal("8.90"))
				.size(PizzaSize.medium)
				.isAvailable(true)
				.build());
	}

	@Test
	void rejectsUnauthenticatedApiRequests() throws Exception {
		mockMvc.perform(post("/api/pizzerias")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Anon Shop","address":"Street","phone":"+351 111 111 111"}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void allowsUnauthenticatedReadOfPublicListings() throws Exception {
		mockMvc.perform(get("/api/pizzerias"))
				.andExpect(status().isOk());
	}

	@Test
	void deniesCustomerWriteActions() throws Exception {
		mockMvc.perform(post("/api/pizzerias")
						.with(userJwt("customer", "CUSTOMER"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Customer Shop","address":"Street","phone":"+351 111 111 111"}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void allowsOwnerToUpdateOwnedPizzeria() throws Exception {
		mockMvc.perform(put("/api/pizzerias/{id}", ownerAPizzeria.getId())
						.with(userJwt("owner-a", "PIZZERIA_OWNER"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Napoli Updated","address":"Rua Nova","phone":"+351 222 222 222"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Napoli Updated"))
				.andExpect(jsonPath("$.ownerSubject").doesNotExist());
	}

	@Test
	void deniesOwnerUpdateOnAnotherOwnersPizzeria() throws Exception {
		mockMvc.perform(put("/api/pizzerias/{id}", ownerBPizzeria.getId())
						.with(userJwt("owner-a", "PIZZERIA_OWNER"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Bad Update","address":"Rua Nova","phone":"+351 222 222 222"}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void allowsAdminToDeleteAnyPizzeria() throws Exception {
		mockMvc.perform(delete("/api/pizzerias/{id}", ownerBPizzeria.getId())
						.with(userJwt("admin", "ADMIN")))
				.andExpect(status().isNoContent());
	}

	@Test
	void filtersInternalOwnershipFieldsFromResponses() throws Exception {
		mockMvc.perform(get("/api/pizzerias/{id}", ownerAPizzeria.getId())
						.with(userJwt("customer", "CUSTOMER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ownerSubject").doesNotExist())
				.andExpect(jsonPath("$.name").value(not("owner-a")));
	}

	@Test
	void exposesOpenApiWithoutBearerToken() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
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
