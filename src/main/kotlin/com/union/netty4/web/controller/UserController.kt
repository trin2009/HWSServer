package com.union.netty4.web.controller

import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import com.union.netty4.dao.IUserRepository
import com.union.netty4.entity.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.*

@RestController
class UserController {
    @Autowired
    private val userRepository: IUserRepository? = null
    @GetMapping("/hi")
    fun home(): String {
        return "hi"
    }

    @GetMapping("/getUserById/{id}")
    fun getUserById(@PathVariable id: Long): Optional<User> {
        return userRepository!!.findById(id)
    }
}