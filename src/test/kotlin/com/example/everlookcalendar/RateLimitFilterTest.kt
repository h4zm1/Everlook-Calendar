package com.example.everlookcalendar

import com.example.everlookcalendar.config.RateLimiterFilter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.concurrent.ConcurrentHashMap

@SpringBootTest(classes = [EverlookCalendarApplication::class])
@AutoConfigureMockMvc
class RateLimitFilterTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var rateLimitFilter: RateLimiterFilter

    // reset buckets before each test so tests don't affect each other
    @BeforeEach
    fun setup() {
        // use reflection to clear the buckets map
        val bucketsField = RateLimiterFilter::class.java.getDeclaredField("buckets")
        bucketsField.isAccessible = true
        val buckets = bucketsField.get(rateLimitFilter) as ConcurrentHashMap<*, *>
        buckets.clear()
    }

    // ==================== PUBLIC ENDPOINT TESTS ====================

    @Test
    fun `public endpoint - should allow requests within limit`() {
        repeat(5) { attempt ->
            mockMvc.perform(
                get("/api/events")
            )
                .andExpect(status().isOk)

            println("event access attempt ${attempt + 1}: allowed")
        }
    }

    @Test
    fun `public endpoint - should block requests after limit`() {
        repeat(10) {
            mockMvc.perform(get("/api/events"))
        }
        mockMvc.perform(get("/api/events"))
            .andExpect(status().isTooManyRequests)

        println("request blocked after limit")
    }

    @Test
    fun `auth endpoint - should block after exceeding limit`() {
        // make 5 requests (the limit)
        repeat(5) { attempt ->
            mockMvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"username": "test@test.com", "password": "wrong"}""")
            )
            println("attempt ${attempt + 1} not blocked")
        }

        // 6th request should be blocked
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username": "test@test.com", "password": "wrong"}""")
        )
            .andExpect(status().isTooManyRequests)  // 429
            .andExpect(jsonPath("$.error").value("Too many requests. Try again later."))

        println("6th request blocked")
    }
    // ==================== PRIVATE ENDPOINT TESTS ====================

}