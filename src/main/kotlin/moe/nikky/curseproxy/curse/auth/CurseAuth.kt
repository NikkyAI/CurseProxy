package moe.nikky.curseproxy.curse.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.runBlocking
import moe.nikky.curseproxy.LOG

import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.io.File

//object AuthToken : KoinComponent {
//    private val mapper: ObjectMapper by inject()
//
//    private var token: String = File("auth.json").bufferedReader().use {
//        mapper.readValue<LoginRequest>(it).token
//    }
//
//    suspend fun authenticate(request: Request) {
//        // add token to header
//        request.headers["AuthenticationToken"] = token // AuthToken.session.token
//    }
//}

suspend fun Request.curseAuth(): Request {
//    AuthToken.authenticate(this)
    return this
}

//data class LoginRequest(
//    @JsonProperty("token") val token: String
//)
