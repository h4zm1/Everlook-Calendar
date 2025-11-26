package com.example.everlookcalendar.controller

import com.example.everlookcalendar.data.ConfigValues
import com.example.everlookcalendar.data.ToggleDebug
import com.example.everlookcalendar.data.UserAuthority
import com.example.everlookcalendar.data.UserCred
import com.example.everlookcalendar.repository.AuthRepo
import com.example.everlookcalendar.repository.ConfigRepo
import com.example.everlookcalendar.repository.RoleRepo
import com.example.everlookcalendar.repository.ToggleDebugRepo
import com.example.everlookcalendar.repository.UserRepo
import com.example.everlookcalendar.service.EmailService
import org.apache.logging.log4j.message.SimpleMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@EnableScheduling
@RestController
@RequestMapping("/conf")
class ConfigController(
    @Autowired val toggleDebugRepo: ToggleDebugRepo,
    private val configRepo: ConfigRepo,
    private val userRepo: UserRepo,
    private val authRepo: AuthRepo,
    private val roleRepo: RoleRepo,
    private val mailSender: JavaMailSender,
    private val emailService: EmailService
) {
    companion object {
        var toggleState = "off"
    }

    data class userToVet(
        val id: Int,
        val email: String,
        val role: String
    )


    @PreAuthorize("hasAuthority('ROLE_GUEST')")
    @PostMapping("/testAuth")
    fun testAuth(@RequestBody data: String): String {
        println("AUTHORIZED " + data)
        return data
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/updateConfig")
    fun updateConfig(@RequestBody configvalues: ConfigValues): ResponseEntity<Any> {
        println("UPDATED CONFIG " + configvalues)
        configRepo.deleteAll()
        configRepo.save(configvalues)

        return ResponseEntity.ok("config updated successfully")
    }

    @PreAuthorize("hasAnyRole('ADMIN','GUEST')")
    @GetMapping("/getConfig")
    fun getConfig(): ConfigValues {
        println("loading config")
        val config = configRepo.findFirstBy()
        return config
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/getToggle")
    fun getToggle(): String {
        val temp = toggleDebugRepo.findAll().first()
        val outVal = if (temp.debug) "on" else "off"
        toggleState = outVal
        return outVal
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/setToggle")
    fun setToggle(): String {
        toggleState = if (toggleState == "on") "off" else "on"
        toggleDebugRepo.deleteAll();
        val newToggle = ToggleDebug()
        newToggle.debug = if (toggleState == "on") true else false
        toggleDebugRepo.save(newToggle)
        return toggleState
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/getUsers")
    fun getUsers(): List<userToVet> {
        return userRepo.findAll().map { user ->
            userToVet(
                id = user.id,
                email = user.username,
                role = user.authorities.first().authority
            )
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/setRole")
    fun updateRole(@RequestBody vettedUser: userToVet): ResponseEntity<Any> {
        println("UPDATED user role " + vettedUser.email + " to " + vettedUser.role)
        // the ground work were meant for a user to have more than one role but had to scale down.
        // find user
        val user = userRepo.findById(vettedUser.id).orElseThrow { RuntimeException("User not found!") }
        // find role of that user
        val userAuth = authRepo.findByUser(user)
        // delete that role
        authRepo.deleteById(userAuth.id.toInt())
        //  find the role by it's name
        val newRole = roleRepo.findByName(vettedUser.role).orElseThrow { RuntimeException("Role not found!") }
        // save new role to user
        authRepo.save(UserAuthority(user, newRole))
        if (vettedUser.role == "ROLE_GUEST" || vettedUser.role == "ROLE_ADMIN")
            emailService.sendMail(vettedUser.email)
        return ResponseEntity.ok("config updated successfully")
    }

}