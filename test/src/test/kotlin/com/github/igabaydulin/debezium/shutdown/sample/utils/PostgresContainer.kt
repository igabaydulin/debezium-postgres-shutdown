package com.github.igabaydulin.debezium.shutdown.sample.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Container
import org.testcontainers.containers.GenericContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import java.sql.DriverManager
import java.sql.Statement
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@Suppress("SqlResolve", "SqlNoDataSourceInspection")
class PostgresContainer(imageName: Future<String>) : GenericContainer<PostgresContainer>(imageName) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PostgresContainer::class.java)

        fun newInstance(): PostgresContainer {
            return PostgresContainer(
                    ImageFromDockerfile()
                            .withFileFromClasspath("Dockerfile", "docker/postgresql/Dockerfile")
                            .withFileFromClasspath("init.sql", "docker/postgresql/init.sql")
            ).withExposedPorts(5432)
        }
    }

    fun insertQuery() {
        DriverManager.getConnection("jdbc:postgresql://${this.containerIpAddress}:${this.firstMappedPort}/foo", "alice", "123456").use {
            log.debug("Connection established")
            val statement: Statement = it.createStatement()
            val query = "INSERT INTO bar VALUES (1, 'An infinite number of mathematicians walk into a bar');"
            log.debug("Executing query: {}", query)
            statement.execute(query)
        }
        log.debug("Waiting until connector reads WAL log")
        TimeUnit.SECONDS.sleep(10)
    }

    fun shutdown() {
        log.debug("Shutting down postgres")
        val result: Container.ExecResult = this.execInContainer(
                "/usr/lib/postgresql/12/bin/pg_ctl",
                "stop",
                "-D",
                "/var/lib/postgresql/data")
        log.debug("Result $result")
    }
}