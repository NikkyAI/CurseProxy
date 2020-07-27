package moe.nikky.curseproxy.di

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import moe.nikky.curseproxy.data.CurseDAO
import org.koin.dsl.module

@OptIn(UnstableDefault::class)
val mainModule = module(moduleDeclaration = {
    single {
        jacksonObjectMapper() // Enable JSON parsing
            .registerModule(KotlinModule()) // Enable Kotlin support
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    }
    single {
        Json(JsonConfiguration(ignoreUnknownKeys = true))
    }
//    single { setupCurseDatabase("curse.db") }
    single { CurseDAO("curse.db") }
//    provide(kind = Kind.Single) { AppSchema(get()) }
})