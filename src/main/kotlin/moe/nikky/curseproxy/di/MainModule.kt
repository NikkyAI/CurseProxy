package moe.nikky.curseproxy.di

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import moe.nikky.curseproxy.curse.auth.AuthToken
import moe.nikky.curseproxy.dao.AddonDatabase
import moe.nikky.curseproxy.dao.AddonStorage
import moe.nikky.curseproxy.graphql.AppSchema
import org.koin.dsl.module.applicationContext

val mainModule = applicationContext {
    provide { Gson() }
    provide {
        jacksonObjectMapper() // Enable JSON parsing
                .registerModule(KotlinModule()) // Enable Kotlin support
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    }
    provide { AppSchema(get()) }
    provide { AddonDatabase() as AddonStorage }
//    provide { AuthToken() }
}