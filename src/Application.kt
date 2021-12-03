package com.example

import com.squareup.sqldelight.TransacterImpl
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module() {
    val config = HikariConfig().apply {
        val dbFile = File(".database/db").absoluteFile
        dbFile.parentFile.deleteRecursively()
        dbFile.parentFile.mkdirs()
        jdbcUrl = "jdbc:h2:file:./.database/db;MODE=MySQL"
        username = "userName"
        password = "pass"
    }
    val dataSource = HikariDataSource(config)
    val driver = dataSource.asJdbcDriver()
    val transacter = SqlDriverTransacter(driver)

    try {
        transacter.transaction {
            driver.execute(null, "CREATE TABLE throw_test(some Text)", 0, null)
            throw ExpectedException()
        }
    } catch (_: ExpectedException) {
        println("expected throw")
        transacter.transaction {
            driver.execute(null, "CREATE TABLE throw_test(some Text)", 0, null)
        }
    }
}

private class ExpectedException : Exception()
private class SqlDriverTransacter(driver: SqlDriver) : TransacterImpl(driver)

