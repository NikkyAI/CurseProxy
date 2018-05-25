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

    var response: LoginResponse = login()

    fun test() {
        logger.log("renewAfter: ${response.session.renewAfter}")
        logger.log("expires:    ${response.session.expires}")
        logger.log("now:        ${System.currentTimeMillis()}")
    }

    fun login(): LoginResponse {
        val url = "$AUTH_API/login"

        val body: LoginRequest = File("auth.json").bufferedReader().use {
            mapper.readValue(it)
        }

        val (request, response, result) = url.httpPost()
                .body(mapper.writeValueAsBytes(body))
                .header("Content-Type" to "application/json")
//                    .body()
                .responseString()
        when (result) {
            is Result.Success -> {
                logger.log("json: ${result.value}")
                return mapper.readValue(result.value)
            }
            is Result.Failure -> {
                logger.log("failed $request ${result.error}")
                throw RuntimeException("login failure")
            }
        }
    }

}


fun Request.curseAuth(): Request {

    val now = System.currentTimeMillis()
    if (AuthToken.response.session.renewAfter < now || AuthToken.response.session.expires < now) {
        AuthToken.response = AuthToken.login()
    }

    // add token to header
    this.headers["AuthenticationToken"] = AuthToken.response.session.token

    return this
}

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
        @JsonProperty("Token") val token: String,
        @JsonProperty("EmailAddress") val emailAddress: String,
        @JsonProperty("EffectivePremiumStatus") val effectivePremiumStatus: Boolean,
        @JsonProperty("ActualPremiumStatus") val actualPremiumStatus: Boolean,
        @JsonProperty("SubscriptionToken") val subscriptionToken: Int,
        @JsonProperty("Expires") val expires: Long,
        @JsonProperty("RenewAfter") val renewAfter: Long,
        @JsonProperty("IsTemporaryAccount") val isTemporaryAccount: Boolean,
        @JsonProperty("IsMerged") val isMerged: Boolean,
        @JsonProperty("Bans") val bans: Int
)
