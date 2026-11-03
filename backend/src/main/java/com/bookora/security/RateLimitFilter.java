package com.bookora.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Lets each client address make a limited number of requests per minute; the next ones get 429 until the minute is over. */
public class RateLimitFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

	private static final int MAX_TRACKED_CLIENTS = 10_000;

	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
	private final int requestsPerMinute;

	public RateLimitFilter(int requestsPerMinute) {
		this.requestsPerMinute = requestsPerMinute;
	}

	/** Covers are small cached images the page loads in bulk, so they do not count against a visitor. */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getRequestURI().startsWith("/api/covers/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		ConsumptionProbe probe = bucketFor(request.getRemoteAddr()).tryConsumeAndReturnRemaining(1);
		if (probe.isConsumed()) {
			if (probe.getRemainingTokens() == 0) {
				log.warn("Rate limit reached: client={}", request.getRemoteAddr());
			}
			chain.doFilter(request, response);
			return;
		}
		long seconds = Math.max(1, Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds());
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setHeader("Retry-After", String.valueOf(seconds));
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write("{\"title\":\"Too Many Requests\",\"status\":429,"
				+ "\"detail\":\"Too many requests. Please wait " + seconds + " seconds and try again.\"}");
	}

	private Bucket bucketFor(String client) {
		if (buckets.size() >= MAX_TRACKED_CLIENTS) {
			buckets.values().removeIf(bucket -> bucket.getAvailableTokens() >= requestsPerMinute);
		}
		return buckets.computeIfAbsent(client, key -> Bucket.builder()
				.addLimit(limit -> limit.capacity(requestsPerMinute).refillIntervally(requestsPerMinute, Duration.ofMinutes(1)))
				.build());
	}

}
