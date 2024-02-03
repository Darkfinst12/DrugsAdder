plugins {
    java
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.5.10"
    id("xyz.jpenilla.run-paper") version "2.2.2" // Adds runServer and runMojangMappedServer tasks for testing
}

repositories {
    mavenLocal()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.md-5.net/content/groups/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://nexus.hc.to/content/repositories/pub_releases")
    maven("https://jitpack.io")
    maven("https://maven.enginehub.org/repo/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")

    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.projectlombok:lombok:1.18.28")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.github.Slimefun:Slimefun4:RC-35")

    annotationProcessor("org.projectlombok:lombok:1.18.28")

    compileOnly("org.projectlombok:lombok:1.18.28")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.15")
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.15")
    compileOnly("com.sk89q.worldguard:worldguard-core:7.0.9")
    compileOnly("net.Indyuce:MMOItems-API:6.9.2-SNAPSHOT")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        val props = mapOf(
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
                "apiVersion" to "1.20"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

group = "de.DrugsAdder"
version = "1.0-SNAPSHOT"
description = "DrugsAdder"
java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}