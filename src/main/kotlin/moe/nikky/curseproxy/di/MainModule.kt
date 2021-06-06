package moe.nikky.curseproxy.di

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.generator.toSchema
import com.expediagroup.graphql.server.execution.*
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import get
import graphql.GraphQL
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLType
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.request.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import moe.nikky.curseproxy.data.CurseDAO
import moe.nikky.curseproxy.graphql.KtorDataLoaderRegistryFactory
import moe.nikky.curseproxy.graphql.KtorGraphQLContextFactory
import moe.nikky.curseproxy.graphql.KtorGraphQLRequestParser
import moe.nikky.curseproxy.graphql.schema.CurseQueryService
import org.koin.dsl.bind
import org.koin.dsl.module
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KType

val mainModule = module(moduleDeclaration = {
    single {
        jacksonObjectMapper() // Enable JSON parsing
            .registerModule(KotlinModule()) // Enable Kotlin support
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    }
    single {
        Json {
            ignoreUnknownKeys = true
            serializersModule = SerializersModule  {

            }
        }
    }
    single {
        HttpClient(OkHttp) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.NONE
            }
            install(UserAgent) {
                agent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) twitch-desktop-electron-platform/1.0.0 Chrome/73.0.3683.121 Electron/5.0.12 Safari/537.36 desklight/8.51.0"
            }
            install(JsonFeature) {
                serializer = KotlinxSerializer(
                    json = get()
                )
            }
        }
    }
    single { CurseDAO("curse.db") }

    single {
        CurseQueryService(
            curseDAO = get()
        )
    }
    single {
        object: SchemaGeneratorHooks {
            override fun willGenerateGraphQLType(type: KType): GraphQLType? = when(type.classifier as? KClass<*>){
                LocalDateTime::class -> ExtendedScalars.DateTime
                else -> null
            }
        }
    } bind SchemaGeneratorHooks::class
    single {
        val config = SchemaGeneratorConfig(
            supportedPackages = listOf("moe.nikky.curseproxy"),
            hooks = get()
        )
        val queries = listOf(
            TopLevelObject(get<CurseQueryService>())
        )
        val mutations = listOf<TopLevelObject>(
//            TopLevelObject(LoginMutationService())
        )
        val graphQLSchema = toSchema(config, queries, mutations)

        GraphQL.newGraphQL(graphQLSchema)
            .build()
    }
    single {
        KtorDataLoaderRegistryFactory()
    } bind DataLoaderRegistryFactory::class
    single {
        KtorGraphQLRequestParser(get())
    } bind GraphQLRequestParser::class
    single {
        KtorGraphQLContextFactory()
    } bind GraphQLContextFactory::class
    single {
        GraphQLRequestHandler(
            graphQL = get(),
            dataLoaderRegistryFactory = get()
        )
    }
    single {
        GraphQLServer<ApplicationRequest>(
            requestParser = get(),
            contextFactory = get(),
            requestHandler = get()
        )
    }

})