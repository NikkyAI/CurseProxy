package moe.nikky.curseproxy

import com.expediagroup.graphql.server.execution.GraphQLServer
import com.expediagroup.graphql.server.types.GraphQLServerResponse
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.call
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {  }
fun Route.graphql() {

    val mapper by koinCtx.inject<ObjectMapper>()
    val ktorGraphQLServer by koinCtx.inject<GraphQLServer<ApplicationRequest>>()
//    graphQL(
//            "path/to/schema.graphqls",
//            MyQueryResolver()
//    )
    route("/graphql") {

        post {

            // Execute the query against the schema
            val result: GraphQLServerResponse? = ktorGraphQLServer.execute(call.request)

            if (result != null) {
                // write response as json
                val json = mapper.writeValueAsString(result)
                call.response.call.respond(json)
            } else {
                call.response.call.respond(HttpStatusCode.BadRequest, "Invalid request")
            }

//            val request = call.receive<GraphQLRequest>()
//
//            val query = request.query
//            logger.info{"the graphql query: $query"}
//            logger.info{"request.variables: ${request.variables}"}
//
//            val variables = mapper.writeValueAsString(request.variables ?: emptyMap<String, Any>())
//            logger.info{"the graphql variables: $variables"}
//            val response = measureMillisAndReport("execute schema", logger::info) {
//                schema.execute(query, variables)
//            }
//            call.respondText(response, ContentType.Application.Json)
        }
    }
}