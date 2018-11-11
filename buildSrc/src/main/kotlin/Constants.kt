import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.wrapper.Wrapper

object Gradle {
    const val version = "4.10.2"
    val distributionType = Wrapper.DistributionType.ALL
}

object Kotlin {
    const val version = "1.3.0"
}

object Ktor {
    const val version = "1.0.0-beta-3" //""0.9.4"
}

object Logback {
    const val version = "1.2.3"
}

object Fuel {
    const val version = "1.16.0"
}

object KGraphQL {
    const val version = "772afa793f718f9643b58f486a0e49bf10799c9a" //"0.3.0-beta"
}

object Koin {
    const val version = "1.0.1"
}

object Squash {
    const val version = "0.2.2"
}

object Tomcat {
    const val version = "9.0.11"
}