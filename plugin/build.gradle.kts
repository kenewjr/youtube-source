import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    `java-library`
    alias(libs.plugins.lavalink.gradle.plugin)
    alias(libs.plugins.maven.publish.base)
}

lavalinkPlugin {
    name = "youtube-plugin"
    path = "dev.lavalink.youtube.plugin"
    apiVersion = libs.versions.lavalink
    serverVersion = libs.versions.lavalink.get()
    configurePublishing = false
}

base {
    archivesName = "youtube-plugin"
}

dependencies {
    implementation(projects.common)
    implementation(projects.v2)
    compileOnly(libs.lavalink.server)
    compileOnly(libs.lavaplayer.ext.youtube.rotator)
    implementation(libs.rhino.engine)
    implementation(libs.nanojson)
    compileOnly(libs.slf4j)
    compileOnly(libs.annotations)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

mavenPublishing {
    coordinates("dev.lavalink.youtube", "youtube-plugin", version.toString())
    configure(JavaLibrary(JavadocJar.None(), sourcesJar = false))
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(":common:compileTestJava", ":common:jar", ":v2:jar")
}

tasks {
    test {
        useJUnitPlatform()
    }
    processResources {
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "version" to project.version
            )
        )
    }
}
