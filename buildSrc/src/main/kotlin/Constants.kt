import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.wrapper.Wrapper

object Gradle {
    const val version = "5.0-milestone-1"
    val distributionType = Wrapper.DistributionType.ALL
}

object Kotlin {
    const val version = "1.3.0-rc-116"
//    const val version = "1.3.0-rc-190"
}

object Ktor {
    const val version = "1.0.0-beta-1" //""0.9.4"
}

object Logback {
    const val version = "1.2.3"
}

object Fuel {
    const val version = "1.16.0"
}

object KGraphQL {
//    const val version = "0.2.8"
    const val version = "0.3.0-breta"
//    const val version = "0.3.0-alpha"
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