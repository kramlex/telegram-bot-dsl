buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath(libs.kotlinSerializationGradle)
        classpath(libs.sqlDelightGradle)
        classpath(":build-logic")
    }
}

tasks.register("clean", Delete::class).configure {
    group = "build"
    delete(rootProject.buildDir)
}
