package com.union.netty4.entity

import javax.persistence.*

@Entity
@Table(name = "user")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: Long = 0L

    @Column
    private var name: String = ""

    @Column
    private var password: String = ""
}