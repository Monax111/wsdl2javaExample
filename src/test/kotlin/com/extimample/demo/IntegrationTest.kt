package com.extimample.demo

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.extensions.testcontainers.perSpec
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer

class IntegrationTest : FeatureSpec({

    val redis = GenericContainer<Nothing>("redis:5.0.3-alpine").apply {
        withExposedPorts(6379)
        start()
    }
    listener(redis.perSpec()) //converts container to listener and registering it with Kotest.

    val postgre = PostgreSQLContainer<Nothing>("postgres:11.1").apply {
        start()
    }
    listener(postgre.perSpec()) //converts container to listener and registering it with Kotest.

    val redisHost = redis.containerIpAddress + ":" + redis.firstMappedPort
    val image = System.getProperty("image", "demo:local")
    val mockProxy = GenericContainer<Nothing>(image).apply {
        withEnv("spring.r2dbc.url", postgre.jdbcUrl)
        withEnv("spring.r2dbc.username", postgre.username)
        withEnv("spring.r2dbc.password", postgre.password)

        withEnv("datasources.redis.nodes", redisHost)
        dependsOn(redis, postgre)
        start()
    }
    listener(redis.perSpec()) //converts container to listener and registering it with Kotest.


    feature("get a default mock") {
        scenario("demo") {

            println(redisHost)

            Thread.sleep(20000)

            println(mockProxy.logs)

            val a = 123


        }
    }

})