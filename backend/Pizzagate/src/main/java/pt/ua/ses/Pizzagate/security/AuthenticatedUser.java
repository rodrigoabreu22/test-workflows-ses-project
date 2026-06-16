package pt.ua.ses.Pizzagate.security;

import java.util.Set;

public record AuthenticatedUser(String subject, Set<String> roles) {
}
