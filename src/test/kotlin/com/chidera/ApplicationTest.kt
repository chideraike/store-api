package com.chidera

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.features.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import kotlin.test.*
import io.ktor.server.testing.*
import com.chidera.plugins.*
import org.junit.Test

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Welcome to Shop World!", response.content)
            }
        }
    }
    @Test
    fun getAllShops() {
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/shop").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
    @Test
    fun getShopById() {
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/shop/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}