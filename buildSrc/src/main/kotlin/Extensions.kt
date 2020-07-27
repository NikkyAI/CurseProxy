package org.gradle.kotlin.dsl

import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.ktor(module: String? = null, version: String? = null): Any =
    "io.ktor:${module?.let { "ktor-$module" } ?: "ktor"}:${version ?: "_"}"
