package moe.nikky.curseproxy

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.pgutkowski.kgraphql.schema.Schema
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import org.slf4j.Logger

@Location("/graphql")
data class GraphQLRequest(val query: String = "", val variables: Map<String, Any>? = emptyMap(), val operationName: String? = null)

fun Route.graphql(log: Logger, mapper: ObjectMapper, schema: Schema) {
//    graphQL(
//            "path/to/schema.graphqls",
//            MyQueryResolver()
//    )
    post<GraphQLRequest> {
        val request = call.receive<GraphQLRequest>()

        val query = request.query
        log.info("the graphql query: $query")
        log.info("request.variables: ${request.variables}")

        val variables = mapper.writeValueAsString(request.variables ?: emptyMap<String, Any>())
        log.info("the graphql variables: $variables")

        call.respondText(schema.execute(query, variables), ContentType.Application.Json)
    }
}