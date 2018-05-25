package moe.nikky.curseproxy.curse.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import org.koin.Koin.Companion.logger

import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.io.File

object AuthToken : KoinComponent {
    private val mapper: ObjectMapper by inject()
    val AUTH_API = "https://logins-v1.curseapp.net"

    var session: Session = login()
        private set

    fun test() {
        logger.log("renewAfter: ${session.renewAfter}")
        logger.log("expires:    ${session.expires}")
        logger.log("now:        ${System.currentTimeMillis()}")
    }

    private fun login(): Session {
        val url = "$AUTH_API/login"

        val body: LoginRequest = File("auth.json").bufferedReader().use {
            mapper.readValue(it)
        }

        val (request, response, result) = url.httpPost()
                .header("Content-Type" to "application/json")
                .body(mapper.writeValueAsBytes(body))
                .responseString()
        val loginResponse: LoginResponse = when(result) {
            is Result.Success -> {
                logger.log("login json: ${result.value}")
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                logger.log("failed $request $response ${result.error}")
                throw RuntimeException("login failure")
            }
        }
        return loginResponse.session
    }

    private fun renew(): Session {
        val url = "$AUTH_API/login/renew"

        val (request, response, result) = url.httpPost()
                .header("Content-Type" to "application/json", "AuthenticationToken" to session.token)
//                .body(mapper.writeValueAsBytes(body))
                .responseString()
        val renewResponse: RenewTokenResponseContract = when (result) {
            is Result.Success -> {
                logger.log("renew json: ${result.value}")
                mapper.readValue(result.value)
            }
            is Result.Failure -> {
                logger.log("failed $request $response ${result.error}")
                throw RuntimeException("login failure")
            }
        }
        session.token = renewResponse.token
        session.expires = renewResponse.expires
        session.renewAfter = renewResponse.renewAfter
        return session
    }

    fun authenticate(request: Request) {

        val now = System.currentTimeMillis()
        if (session.renewAfter < now) {
            session = AuthToken.renew()
        } else if (AuthToken.session.expires < now) {
            session = AuthToken.login()
        }

        // add token to header
        request.header("AuthenticationToken" to AuthToken.session.token)
    }

//    fun authenticate(request: Request, forceRenew: Boolean) {
//
//        val now = System.currentTimeMillis()
//        if (session.renewAfter < now || forceRenew) {
//            session = AuthToken.renew()
//        } else if (AuthToken.session.expires < now) {
//            session = AuthToken.login()
//        }
//
//        // add token to header
//        request.header("AuthenticationToken" to AuthToken.session.token)
//    }

}


fun Request.curseAuth(): Request {
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
