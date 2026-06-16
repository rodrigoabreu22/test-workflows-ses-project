package pt.ua.ses.Pizzagate.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supported pizza sizes")
public enum PizzaSize {
	small,
	medium,
	large
}
