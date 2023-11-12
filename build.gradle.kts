plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.5.5"
    id("xyz.jpenilla.run-paper") version "2.1.0" // Adds runServer and runMojangMappedServer tasks for testing
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.md-5.net/content/groups/public/")
    }

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://nexus.hc.to/content/repositories/pub_releases")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }

    maven {
        url = uri("https://nexus.phoenixdevt.fr/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")

    implementation("org.jetbrains:annotations:24.0.0")

    implementation("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    compileOnly("org.projectlombok:lombok:1.18.28")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.github.Slimefun:Slimefun4:RC-35")

    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.15")
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.15")
    compileOnly("com.sk89q.worldguard:worldguard-core:7.0.9")
    compileOnly("net.Indyuce:MMOItems-API:6.9.2-SNAPSHOT")

    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.1")
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
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

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}