package com.extimample.demo

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.extensions.testcontainers.perSpec
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network.newNetwork
import org.testcontainers.containers.PostgreSQLContainer

class IntegrationNetworkTest : FeatureSpec({

    val network = newNetwork()

    val redis = GenericContainer<Nothing>("redis:5.0.3-alpine").apply {
        withNetwork (network)
        withNetworkAliases("redis")
        start()
    }
    listener(redis.perSpec()) //converts container to listener and registering it with Kotest.

    val postgre = PostgreSQLContainer<Nothing>("postgres:11.1").apply {
        withNetwork (network)
        withNetworkAliases("postgre")
        start()
    }
    listener(postgre.perSpec()) //converts container to listener and registering it with Kotest.

    val image = System.getProperty("image", "demo:local")
    val mockProxy = GenericContainer<Nothing>(image).apply {
        withNetwork (network)
        withEnv("spring.r2dbc.url", "r2dbc:postgresql://"+postgre.networkAliases.first()+":6379")
        withEnv("spring.r2dbc.username", postgre.username)
        withEnv("spring.r2dbc.password", postgre.password)
        withEnv("datasources.redis.nodes", redis.networkAliases.first()+":6379")
        withExposedPorts(8080)
        dependsOn(redis, postgre)
        start()
    }
    listener(redis.perSpec()) //converts container to listener and registering it with Kotest.


    feature("get a default mock") {
        scenario("demo") {

            Thread.sleep(20000)

            println(mockProxy.logs)

            val a = 123


        }
    }

})