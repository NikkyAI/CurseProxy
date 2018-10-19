package moe.nikky.curseproxy.di

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import moe.nikky.curseproxy.dao.AddonDatabase
import moe.nikky.curseproxy.dao.AddonStorage
import moe.nikky.curseproxy.graphql.AppSchema
import org.koin.dsl.definition.Kind
import org.koin.dsl.module.module

val mainModule = module(definition = {
    provide(kind = Kind.Single) {
        jacksonObjectMapper() // Enable JSON parsing
            .registerModule(KotlinModule()) // Enable Kotlin support
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    }
    provide(kind = Kind.Single) { AppSchema(get()) }
    provide(kind = Kind.Single) { AddonDatabase() as AddonStorage }
})