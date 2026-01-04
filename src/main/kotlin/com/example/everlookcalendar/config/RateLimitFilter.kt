package com.example.everlookcalendar.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap


@Component
class RateLimiterFilter : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(javaClass)

    // store bucket for each ip/mail
    // key: ip address or mail (if authenticated)
    // value: their token bucket
    private val buckets = ConcurrentHashMap<String, Bucket>()

    // this shouldn't really matter on prod since static files will get handled by nginx or cloudflare
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI

        return path.startsWith("/api/time") || //handled manually in it's endpoint
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/auth/refreshtoken") ||
                path.startsWith("/images/") ||
                path.startsWith("/static/") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".png") ||
                path.endsWith(".ico")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // get current authentication from spring security
        val auth = SecurityContextHolder.getContext().authentication
        logger.warn("***request hit Spring Boot: ${request.requestURI}" + " auth:: " + auth?.authorities?.first())
        // get the path
        val path = request.requestURI
        // determine who's making the request
        // if logged in AND not anonymouseUser: use their mail/username
        // if not: use their ip address
        // ps: spring security will create an auth object for unauthenticated users
        // so auth.isAuthenticated will return true for them (default behavior smh)
        val userKey = if (auth?.isAuthenticated == true && auth.name != "anonymousUser") {
            auth.name
        } else {
            request.remoteAddr
        }
        // combine both ip/mail with path for seperate bucket per endpoint
        val key = "$userKey:$path"
        // get existing bucket or create new one for this key/mail + path combo
        // computeIfAbsent is thread safe: only create bucket if they don't exist
        val bucket = buckets.computeIfAbsent(key) { createBucket(auth, path) }

        // try and consume 1 token from the bucket
        // return true if tokens available, false if hit limit
        if (bucket.tryConsume(1)) {
            // tokens available: allow request to proceed
            logger.warn("consumed 1 token, left=" + bucket.availableTokens + " at: " + path)
            filterChain.doFilter(request, response)
        } else {
            // no tokens left: block the request
            response.status = 429
            response.contentType = "application/json"
            response.writer.write("""{"error": "Too many requests. Try again later."}""")
        }
    }

    /**
     * create token bucket based on role/path
     * -buckets hold x tokens (capacity)
     * -each request consumes 1 token
     * -tokens refill overtime
     * -if bucket is empty, request then is blocked
     */
    private fun createBucket(auth: Authentication?, path: String): Bucket {
        // determine rate limit based on on role/path
        // more restricted role get more tokens and opposite for paths (more specific paths get less tokens)
        val (capacity, duration) = when {
            path.startsWith("/auth/login") -> Pair(5L, Duration.ofMinutes(15))
            path.startsWith("/auth/register") -> Pair(3L, Duration.ofHours(1))
            path.startsWith("/api/events") -> Pair(20L, Duration.ofMinutes(15))
            path.startsWith("/api/zgboss") -> Pair(20L, Duration.ofMinutes(15))
            auth?.authorities?.any { it.authority == "ROLE_ADMIN" } == true -> {
                Pair(1000L, Duration.ofHours(1))
            }

            auth?.authorities?.any { it.authority == "ROLE_USER" || it.authority == "ROLE_GUEST" } == true -> {
                Pair(20L, Duration.ofHours(1))
            }

            path.startsWith("/auth/status") -> Pair(20L, Duration.ofMinutes(15))


            else -> Pair(20L, Duration.ofHours(1))
        }


        // built bucket with the limits
        val bandwidth = Bandwidth.builder()
            .capacity(capacity)
            .refillIntervally(capacity, duration)
            .build()
        return Bucket.builder().addLimit(bandwidth).build()
    }


}
