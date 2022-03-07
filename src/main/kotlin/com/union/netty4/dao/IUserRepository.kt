package com.union.netty4.dao

import com.union.netty4.entity.User
import org.springframework.data.repository.CrudRepository

interface IUserRepository : CrudRepository<User, Long>