package moe.nikky.curseproxy

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.pgutkowski.kgraphql.schema.Schema
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route
import org.slf4j.Logger

@Location("/graphql")
data class GraphQLRequest(val query: String = "", val variables: Map<String, Any> = emptyMap())

fun Route.graphql(log: Logger, gson: ObjectMapper, schema: Schema) {
//    graphQL(
//            "path/to/schema.graphqls",
//            MyQueryResolver()
//    )
    post<GraphQLRequest> {
        val request = call.receive<GraphQLRequest>()

        val query = request.query
        log.info("the graphql query: $query")

        val variables = gson.writeValueAsString(request.variables)
        log.info("the graphql variables: $variables")

        call.respondText(schema.execute(query, variables), ContentType.Application.Json)
    }
}