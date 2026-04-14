import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("java")
    kotlin("jvm") version "2.3.10"
    alias(libs.plugins.orion)
    alias(libs.plugins.shadow)
    alias(libs.plugins.unimined)
    alias(libs.plugins.modpublisher)
    id("maven-publish")
}

orion.setup {
    enableMirrorMaven.set(true)
    enableReleasesMaven.set(true)
    enableSnapshotsMaven.set(true)
    multiProject.set(true)

    dopplerToken = System.getenv("DOPPLER_KEY")

    versioning {
        val ident = project.properties["releaseType"] ?: orion.getProperty("release_type")
        identifier(ident.toString())
    }

    tools {
        lombok()
        noLoader()
    }
}

tasks.jar { enabled = false }

subprojects {
    apply(plugin = "dev.firstdark.unimined")
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.hypherionmc.modutils.orion")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "com.hypherionmc.modutils.modpublisher")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = orion.getProperty("project_group")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        mavenCentral()
        orion.unimaven()
    }

    val shade by configurations.creating

    configurations {
        implementation.get().extendsFrom(shade)
    }

    dependencies {
        // Discord
        shade("pw.chew:jda-chewtils:${orion.getProperty("chewtils")}") {
            exclude(group = "org.apache.commons")
        }
        shade("net.dv8tion:JDA:${orion.getProperty("jda")}") {
            exclude(module = "opus-java")
            exclude(group = "org.apache.commons")
        }
        shade("club.minnced:discord-webhooks:${orion.getProperty("webhooks")}") {}

        // Utilities
        shade("org.apache.commons:commons-collections4:${orion.getProperty("commons4")}")
        shade("com.github.oshi:oshi-core:${orion.getProperty("oshi")}")
        shade("org.jasypt:jasypt:${orion.getProperty("jasypt")}:lite")
        shade("net.fellbaum:jemoji:1.6.0")

        implementation("com.hypherionmc.craterlib:CraterLib-API:${orion.getProperty("craterlib")}")
        implementation("unimaven.nightbloom:mmode:${orion.getProperty("maintenance_mode")}")
        implementation("io.github.joagar21:GuildsCobblemon:1.0.2:api")
    }

    /*
     * ===============================================================================
     * =       DO NOT EDIT BELOW THIS LINE UNLESS YOU KNOW WHAT YOU ARE DOING        =
     * ===============================================================================
     */
    unimined.minecraft {
        version(orion.getProperty("minecraft_version"))

        fabric {
            loader(orion.getProperty("fabric_loader"))
        }

        mappings {
            mojmap()
            devNamespace("mojmap")
        }

        defaultRemapJar = false
    }

    tasks.withType(ProcessResources::class.java).configureEach {
        if (project.name !== "Common") {
            from(project(":Common").sourceSets.main.get().resources)
        }
        val buildProps = project.properties.toMap()

        filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml", "paper-plugin.yml")) {
            expand(buildProps)
        }
    }

    if (project.name !== "Common") {
        tasks.withType(JavaCompile::class.java).configureEach {
            source(project(":Common").sourceSets.main.get().allSource)
        }

        tasks.withType(KotlinCompile::class.java).configureEach {
            source(project(":Common").sourceSets.main.get().allSource)
        }
    }

    tasks.named("compileTestJava") { enabled = false }
    tasks.named("compileTestKotlin") { enabled = false }

    tasks.shadowJar {
        from(sourceSets.main.get().output)
        configurations = listOf(shade)
        archiveClassifier.set("")
        addMultiReleaseAttribute.set(false)

        dependencies {
            exclude(dependency("org.apache.logging.log4j:log4j-core:.*"))
            exclude(dependency("org.apache.logging.log4j:log4j-core:.*"))
            exclude(dependency("org.apache.logging.log4j:log4j-slf4j18-impl:.*"))
            exclude(dependency("org.apache.commons:commons-lang3:.*"))
            exclude(dependency("com.google.code.gson:.*"))
            exclude(dependency("javax:.*"))
            exclude(dependency("org.jetbrains:.*"))
            exclude(dependency("net.java.dev.jna:.*"))
            exclude(dependency("org.slf4j:.*"))

            exclude("org/slf4j/**")
            exclude("META-INF/versions/9/**")
            exclude("module-info.class")
            exclude("org/apache/commons/lang3/**")

            relocate("org.apache.commons.collections4", "${orion.getProperty("shade_group")}.apache.commons.collections4")
            relocate("javax.annotation", "${orion.getProperty("shade_group")}.javax.annotation")
            relocate("gnu.trove", "${orion.getProperty("shade_group")}.gnu.trove")
            relocate("com.fasterxml", "${orion.getProperty("shade_group")}.fasterxml")
            relocate("club.minnced", "${orion.getProperty("shade_group")}.club.minnced")
            relocate("com.iwebpp", "${orion.getProperty("shade_group")}.iwebpp")
            relocate("com.jagrosh", "${orion.getProperty("shade_group")}.jagrosh")
            relocate("com.neovisionaries", "${orion.getProperty("shade_group")}.neovisionaries")
            relocate("net.dv8tion", "${orion.getProperty("shade_group")}.dv8tion")
            relocate("okhttp3", "${orion.getProperty("shade_group")}.okhttp3")
            relocate("okio", "${orion.getProperty("shade_group")}.okio")
            relocate("org.json", "${orion.getProperty("shade_group")}.json")
            relocate("pw.chew", "${orion.getProperty("shade_group")}.chew")
            relocate("oshi", "${orion.getProperty("shade_group")}.oshi")
            relocate("kotlin", "${orion.getProperty("shade_group")}.kotlin")
            relocate("org.jasypt", "${orion.getProperty("shade_group")}.jasypt")
            relocate("com.google.common", "${orion.getProperty("shade_group")}.google.common")
            relocate("com.google.thirdparty", "${orion.getProperty("shade_group")}.google.thirdparty")
            relocate("edu", "${orion.getProperty("shade_group")}.edu")
            relocate("javassist", "${orion.getProperty("shade_group")}.javassist")
            relocate("org.apache.commons.beanutils", "${orion.getProperty("shade_group")}.org.apache.commons.beanutils")
            relocate("org.apache.commons.collections", "${orion.getProperty("shade_group")}.org.apache.commons.collections")
            relocate("org.apache.commons.jxpath", "${orion.getProperty("shade_group")}.org.apache.commons.jxpath")
            relocate("org.apache.commons.logging", "${orion.getProperty("shade_group")}.org.apache.commons.logging")
            relocate("org.reflections", "${orion.getProperty("shade_group")}.org.reflections")
            relocate("net.bytebuddy", "${orion.getProperty("shade_group")}.net.bytebuddy")
            relocate("org.checkerframework", "${orion.getProperty("shade_group")}.org.checkerframework")
            relocate("com.google.errorprone", "${orion.getProperty("shade_group")}.com.google.errorprone")
            relocate("net.fellbaum.jemoji", "${orion.getProperty("shade_group")}.net.fellbaum.jemoji")
        }

        exclude("META-INF/maven/**")
        exclude("META-INF/versions/**")
        exclude("META-INF/proguard/**")
        exclude("META-INF/*LICENSE")
        exclude("META-INF/*NOTICE")
        exclude("META-INF/*kotlin_module")
        exclude("META-INF/*.txt")
        exclude("META-INF/services/com.fasterxml.**")

        minimize()
    }

    tasks.shadowJar {
        doLast {
            delete(tasks.jar.get().archiveFile)
        }
    }
    tasks.jar {
        archiveClassifier.set("slim")
        finalizedBy(tasks.named("shadowJar"))

        manifest {
            attributes(mapOf(
                    "Specification-Title"     to orion.getProperty("mod_id"),
                    "Specification-Vendor"    to orion.getProperty("mod_author"),
                    "Specification-Version"   to tasks.jar.get().archiveVersion,
                    "Implementation-Title"    to project.name,
                    "Implementation-Version"  to tasks.jar.get().archiveVersion,
                    "Implementation-Vendor"   to orion.getProperty("mod_author"),
                    "Implementation-Timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")),
                    "Timestamp"               to System.currentTimeMillis(),
                    "Built-On-Java"           to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})",
                    "Built-On-Minecraft"      to orion.getProperty("minecraft_version")
            ))        }
    }
    tasks.withType(JavaCompile::class.java).configureEach {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    tasks.withType(GenerateModuleMetadata::class.java).configureEach {
        enabled = false
    }

    tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(tasks.javadoc)
    }

    tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allJava)
    }
}