package com.github.igabaydulin.debezium.shutdown.sample

import com.github.igabaydulin.debezium.shutdown.sample.utils.ConnectorCallbackHandler
import com.github.igabaydulin.debezium.shutdown.sample.utils.Engines
import com.github.igabaydulin.debezium.shutdown.sample.utils.PostgresContainer
import io.debezium.embedded.EmbeddedEngine
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DebeziumPostgresShutdownTest {

    companion object {
        val postgres: PostgresContainer = PostgresContainer.newInstance()
        val callback: ConnectorCallbackHandler = ConnectorCallbackHandler.newInstance()
    }

    private lateinit var engine: EmbeddedEngine

    @BeforeTest
    fun setUp() {
        postgres.start()
        engine = Engines.defaultEngine(postgres, callback)
    }

    @AfterTest
    fun finish() {
        engine.stop()
    }

    @Test
    fun continue_running_connector_when_postgres_tries_to_shutdown_but_heartbeat_is_not_enabled() {
        Engines.startEngine(engine, callback)
        postgres.insertQuery()
        postgres.shutdown()

        callback.isConnectorStopped.await(30, TimeUnit.SECONDS)
        TimeUnit.SECONDS.sleep(10);
        Assert.assertTrue("Connector is still running", engine.isRunning)
    }
}
