package pt.ua.ses.Pizzagate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class MaxRequestSizeFilter extends OncePerRequestFilter {

	private final long maxBytes;

	public MaxRequestSizeFilter(@Value("${app.security.max-request-bytes:1048576}") long maxBytes) {
		this.maxBytes = maxBytes;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		long contentLength = request.getContentLengthLong();
		if (request.getRequestURI().startsWith("/api/")
				&& contentLength > maxBytes) {
			response.sendError(HttpStatus.PAYLOAD_TOO_LARGE.value(), "Request body is too large.");
			return;
		}
		filterChain.doFilter(request, response);
	}
}
