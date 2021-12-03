package com.example

import com.squareup.sqldelight.TransacterImpl
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import java.io.File
import java.lang.IllegalStateException

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module() {
    val config = HikariConfig().apply {
        val dbFile = File(".database/db").absoluteFile
        if (!dbFile.parentFile.exists()) {
            dbFile.parentFile.mkdirs()
        }
        jdbcUrl = "jdbc:h2:file:./.database/db;MODE=MySQL"
        username = "userName"
        password = "pass"
    }
    val dataSource = HikariDataSource(config)
    val driver = dataSource.asJdbcDriver()
    val transacter = SqlDriverTransacter(driver)
    try {
        transacter.transaction {
            Database.Schema.create(driver)
            throw IllegalStateException()
        }
    } catch (t: Throwable) {
        transacter.transaction {
            Database.Schema.create(driver)
        }
    }
}

private class SqlDriverTransacter(driver: SqlDriver) : TransacterImpl(driver)

