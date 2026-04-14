import com.hypherionmc.modpublisher.properties.ReleaseType

base {
    archivesName.set("${orion.getProperty("mod_name").replace(" ", "")}-Paper")
}

dependencies {
    // Do not edit or remove
    implementation(project(":Common"))
}

tasks.shadowJar {
    dependencies {
        relocate("shadow.kyori", "net.kyori")
    }
}

publisher {
    apiKeys {
        nightbloom(System.getenv("PLATFORM_KEY"))
    }

    nightbloomID.set("sdlink")
    projectVersion.set(project.version.toString())
    setReleaseType(ReleaseType.ALPHA)
    changelog.set(project.rootProject.file("changelog.md"))
    displayName.set("[Paper 1.21 - 1.21.10] Simple Discord Link Paper - ${project.version}")
    setGameVersions("1.21", "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10")
    setLoaders("paper")
    artifact.set(tasks.shadowJar)

    nightbloomDepends {
        required("craterlib")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            group = "com.hypherionmc.sdlink"
            artifactId = project.base.archivesName.get()

            artifact(tasks.shadowJar) {
                builtBy(tasks.shadowJar)
            }
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
        }
    }

    repositories {
        maven(orion.getPublishingMaven())
    }
}