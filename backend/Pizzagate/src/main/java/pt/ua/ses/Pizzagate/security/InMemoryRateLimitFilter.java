package pt.ua.ses.Pizzagate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class InMemoryRateLimitFilter extends OncePerRequestFilter {

	private final int requestsPerMinute;
	private final Clock clock;
	private final ConcurrentMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

	public InMemoryRateLimitFilter(
			@Value("${app.security.rate-limit.requests-per-minute:120}") int requestsPerMinute
	) {
		this.requestsPerMinute = requestsPerMinute;
		this.clock = Clock.systemUTC();
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		if (!request.getRequestURI().startsWith("/api/")) {
			filterChain.doFilter(request, response);
			return;
		}

		long minute = clock.millis() / 60_000;
		String key = request.getRemoteAddr() + ":" + minute;
		WindowCounter counter = counters.compute(key, (ignored, existing) ->
				existing == null || existing.minute() != minute ? new WindowCounter(minute) : existing);

		if (counter.count().incrementAndGet() > requestsPerMinute) {
			response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Rate limit exceeded.");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private record WindowCounter(long minute, AtomicInteger count) {

		private WindowCounter(long minute) {
			this(minute, new AtomicInteger());
		}
	}
}
