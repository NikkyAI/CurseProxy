package moe.nikky.curseproxy.graphql

import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import io.ktor.request.ApplicationRequest

/**
 * Custom logic for how this example app should create its context given the [ApplicationRequest]
 */
class KtorGraphQLContextFactory : GraphQLContextFactory<AuthorizedContext, ApplicationRequest> {

    override suspend fun generateContext(request: ApplicationRequest): AuthorizedContext {
//        val loggedInUser = User(
//            email = "fake@site.com",
//            firstName = "Someone",
//            lastName = "You Don't know",
//            universityId = 4
//        )

        // Parse any headers from the Ktor request
        val customHeader: String? = request.headers["my-custom-header"]

        return AuthorizedContext(/*loggedInUser,*/ customHeader = customHeader)
    }
}