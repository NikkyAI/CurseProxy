/*
 * Copyright 2021 Expedia, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package moe.nikky.curseproxy.graphql;

import com.expediagroup.graphql.server.execution.GraphQLRequestParser
import com.expediagroup.graphql.server.types.GraphQLBatchRequest
import com.expediagroup.graphql.server.types.GraphQLRequest
import com.expediagroup.graphql.server.types.GraphQLServerRequest
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.request.ApplicationRequest
import io.ktor.request.receiveText
import mu.KotlinLogging
import java.io.IOException

/**
 * Custom logic for how Ktor parses the incoming [ApplicationRequest] into the [GraphQLServerRequest]
 */
class KtorGraphQLRequestParser(
    private val mapper: ObjectMapper
) : GraphQLRequestParser<ApplicationRequest> {
    private val logger = KotlinLogging.logger {}
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun parseRequest(request: ApplicationRequest): GraphQLServerRequest = try {
        val rawRequest = request.call.receiveText()
        val query = mapper.readValue(rawRequest, GraphQLServerRequest::class.java)
        when(query) {
            is GraphQLRequest -> {
                logger.info { "operation: ${query.operationName}" }
                logger.info { "query: ${query.query}" }
                logger.info { "variables: ${query.variables}" }
            }
            is GraphQLBatchRequest -> {
                query.requests.forEachIndexed { i, subQuery ->
                    logger.info { "[$i] operation: ${subQuery.operationName}" }
                    logger.info { "[$i] query: ${subQuery.query}" }
                    logger.info { "[$i] variables: ${subQuery.variables}" }
                }
            }
        }
        query
    } catch (e: IOException) {
        throw IOException("Unable to parse GraphQL payload.")
    }
}