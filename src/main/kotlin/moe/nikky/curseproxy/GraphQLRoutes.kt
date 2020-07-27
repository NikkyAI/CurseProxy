package moe.nikky.curseproxy

import com.apurebase.kgraphql.schema.Schema
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receive
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import moe.nikky.curseproxy.util.measureMillisAndReport
import mu.KotlinLogging
import org.slf4j.Logger

data class GraphQLRequest(
    val query: String = "",
    val variables: Map<String, Any>? = emptyMap(),
    val operationName: String? = null
)

fun Route.graphql(mapper: ObjectMapper, schema: Schema) {
    val logger = KotlinLogging.logger {  }
//    graphQL(
//            "path/to/schema.graphqls",
//            MyQueryResolver()
//    )
    route("/graphql") {
        post {
            val request = call.receive<GraphQLRequest>()

            val query = request.query
            logger.info{"the graphql query: $query"}
            logger.info{"request.variables: ${request.variables}"}

            val variables = mapper.writeValueAsString(request.variables ?: emptyMap<String, Any>())
            logger.info{"the graphql variables: $variables"}
            val response = measureMillisAndReport("execute schema", logger::info) {
                schema.execute(query, variables)
            }
            call.respondText(response, ContentType.Application.Json)
        }
    }
}