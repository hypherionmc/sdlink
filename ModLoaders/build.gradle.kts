import com.hypherionmc.modpublisher.properties.ReleaseType

base {
    archivesName.set("${orion.getProperty("mod_name").replace(" ", "")}-Universal")
}

dependencies {
    // Do not edit or remove
    implementation(project(":Common"))
}


publisher {
    apiKeys {
        modrinth(System.getenv("MODRINTH_TOKEN"))
        curseforge(System.getenv("CURSE_TOKEN"))
        nightbloom(System.getenv("PLATFORM_KEY"))
    }

    curseID.set(orion.getProperty("curse_id"))
    modrinthID.set(orion.getProperty("modrinth_id"))
    nightbloomID.set("sdlink")
    projectVersion.set(project.version.toString())
    changelog.set(project.rootProject.file("changelog.md"))
    setReleaseType(ReleaseType.RELEASE)
    displayName.set("[1.18.2-1.21.11] Simple Discord Link Universal - ${project.version}")
    setGameVersions("1.18.2", "1.19.2", "1.20", "1.20.1", "1.21", "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10")
    setLoaders("forge", "fabric", "quilt", "neoforge")
    setCurseEnvironment("both")
    artifact.set(tasks.shadowJar)

    modrinthDepends {
        required("Nn8Wasaq")
    }

    curseDepends {
        required("craterlib")
    }

    nightbloomDepends {
        required("craterlib")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            group = "com.hypherionmc.sdlink"
            artifactId = "sdlink"

            artifact(tasks.shadowJar) {
                builtBy(tasks.shadowJar)
            }
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
            artifact(tasks.named("dashboardZip"))
        }
    }

    repositories {
        maven(orion.getPublishingMaven())
    }
}