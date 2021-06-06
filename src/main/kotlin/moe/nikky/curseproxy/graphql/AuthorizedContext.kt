package moe.nikky.curseproxy.graphql

import com.expediagroup.graphql.generator.execution.GraphQLContext

/**
 * Example of a custom [GraphQLContext]
 */
data class AuthorizedContext(
//    val authorizedUser: User? = null,
    var guestUUID: String? = null,
    val customHeader: String? = null
) : GraphQLContext