package com.example.everlookcalendar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @param:Value("\${spring.mail.username}")
    private val fromEmail: String
) {
    fun sendMail(to: String): ResponseEntity<String> {
        return try {
            val message = SimpleMailMessage().apply {
                setFrom(fromEmail)
                setTo(to)
                subject = ("Access granted")
                text = "If you receive this, you've been granted access!\n" +
                        "You can sign in now."
            }
            mailSender.send(message)
            ResponseEntity.ok("email sent success")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("failed to send mail")
        }
    }
}
