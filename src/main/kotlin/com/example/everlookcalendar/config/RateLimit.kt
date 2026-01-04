package com.example.everlookcalendar.config

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

// BASIC RATE LIMITER (not using it, moved to bucket4j instead)
@Component
class RateLimiter {
    // preventing race conditions:
    // ConcurrentHashMap: allow multiple threads to safly access different part of the map at same time
    // AtomicInt: insure increments to be indivisible (atomic), or uninterrupted
    // @key: ip adress, @value: atomic int counter for thread safe increments
    private val attempts = ConcurrentHashMap<String, AtomicInteger>()

    // store when ip adress should be unblocked
    // @key: ip adress, value: time till block expires (in milliseconds)
    private val blockTime = ConcurrentHashMap<String, Long>()


    // register failed login attemp
    // after 5 attempts, block the ip for 15mins
    // @param key: is the ip adress
    fun registerAttempt(key: String) {
// retrieved or initialize (if absent) a counter for each unique key
        val count = attempts.computeIfAbsent(key) { AtomicInteger(0) }
        // increment counter atomically (thread safe)
        val attemptCount = count.incrementAndGet()
        // check if we hit the limit
        if (attemptCount > 5) {
            // block for 15mins
            blockTime[key] = System.currentTimeMillis() + 900000
            // reset counter since we hit the limit
            attempts.remove(key)
        }
    }

    // check if an IP adress is currently blocked
    // @param key is the ip adress
    // @return true if blocked, false if not
    fun isBlocked(key: String): Boolean {
        val blockedUntil = blockTime[key]

        //  if there's no block entry, it means user isn't blocked
        if (blockedUntil == null) {
            return false
        }

        // if current time is still before the unblock time
        if (System.currentTimeMillis() < blockedUntil) {
            return true // Still blocked
        }

        // this mean there's an entry registered AND current is way past the block time
        // which mean it expired
        blockTime.remove(key)
        return false
    }

    // reset attempt after successful login
    // @param key: ip adress
    fun resetAttempts(key: String) {
        attempts.remove(key)
        // no need to remove from blocktime since it will just expire on it's own
    }
}
