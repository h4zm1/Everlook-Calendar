package com.example.everlookcalendar.service;

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service


@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @param:Value("\${custom_mail}")
    private val fromEmail: String
) {
    fun sendMail(to: String): ResponseEntity<String> {

        return try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true).apply{
                setFrom(fromEmail,"twow-events")
                setTo(to)
                setSubject("Access granted")
                setText("If you receive this, you've been granted access!\n" +
                        "You can sign in now.")
            }
            mailSender.send(message)
            ResponseEntity.ok("email sent success")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("failed to send mail " + e.message)
        }
    }
}
