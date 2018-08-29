package moe.nikky.curseproxy.curse.auth

import awaitStringResponse
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.experimental.runBlocking
import moe.nikky.curseproxy.LOG

import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.io.File

object AuthToken : KoinComponent {
    private val mapper: ObjectMapper by inject()
    private const val AUTH_API = "https://logins-v1.curseapp.net"

    private var session: Session = runBlocking { login() }

    fun test() {
        LOG.info("renewAfter: ${session.renewAfter}")
        LOG.info("expires:    ${session.expires}")
        LOG.info("now:        ${System.currentTimeMillis()}")
    }

    private suspend fun login(): Session {
        val url = "$AUTH_API/login"

        val body: LoginRequest = File("auth.json").bufferedReader().use {
            mapper.readValue(it)
        }

        val (request, response, result) = url.httpPost()
                .apply { headers["Content-Type"] = "application/json" }
                .body(mapper.writeValueAsString(body))
                .awaitStringResponse()
        val loginResponse: LoginResponse = when(result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                LOG.error("failed $request $response ${result.error}")
                throw RuntimeException("login failure")
            }
        }
        return loginResponse.session
    }

    private suspend fun renew(): Session {
        val url = "$AUTH_API/login/renew"

        val (request, response, result) = url.httpPost()
                .header("AuthenticationToken" to session.token)
                .apply { headers["Content-Type"] = "application/json" }
                .awaitStringResponse()
        val renewResponse: RenewTokenResponseContract = when (result) {
            is Result.Success -> {
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                LOG.error("failed $request $response ${result.error}")
                throw RuntimeException("login failure")
            }
        }
        session.token = renewResponse.token
        session.expires = renewResponse.expires
        session.renewAfter = renewResponse.renewAfter
        return session
    }

    suspend fun authenticate(request: Request) {

        val now = System.currentTimeMillis()
        if (session.renewAfter < now) {
            session = AuthToken.renew()
        } else if (AuthToken.session.expires < now) {
            session = AuthToken.login()
        }

        // add token to header
        request.headers["AuthenticationToken"] = AuthToken.session.token
    }

}


suspend fun Request.curseAuth(): Request {
    AuthToken.authenticate(this)
    return this
}

//fun Request.curseAuth(forceRenew: Boolean): Request {
//    AuthToken.authenticate(this)
//    return this
//}

data class LoginRequest(
        @JsonProperty("Username") val username: String,
        @JsonProperty("Password") val password: String
)

data class LoginResponse(
        @JsonProperty("Status") val status: Int,
        @JsonProperty("StatusMessage") val statusMessage: String?,
        @JsonProperty("Session") val session: Session,
        @JsonProperty("Timestamp") val timestamp: Long,
        @JsonProperty("TwitchUsernameReservationToken") val twitchUsernameReservationToken: String
)

data class Session(
        @JsonProperty("UserID") val userID: Int,
        @JsonProperty("Username") val username: String,
        @JsonProperty("DisplayName") val displayName: String?,
        @JsonProperty("SessionID") val sessionID: String,
        @JsonProperty("Token") var token: String,
        @JsonProperty("EmailAddress") val emailAddress: String,
        @JsonProperty("EffectivePremiumStatus") val effectivePremiumStatus: Boolean,
        @JsonProperty("ActualPremiumStatus") val actualPremiumStatus: Boolean,
        @JsonProperty("SubscriptionToken") val subscriptionToken: Int,
        @JsonProperty("Expires") var expires: Long,
        @JsonProperty("RenewAfter") var renewAfter: Long,
        @JsonProperty("IsTemporaryAccount") val isTemporaryAccount: Boolean,
        @JsonProperty("IsMerged") val isMerged: Boolean,
        @JsonProperty("Bans") val bans: Int
)

data class RenewTokenResponseContract(
        @JsonProperty("Token") val token: String,
        @JsonProperty("Expires") val expires: Long,
        @JsonProperty("RenewAfter") val renewAfter: Long
)
