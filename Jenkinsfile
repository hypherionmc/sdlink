pipeline {
    agent {
        label "master"
    }
    tools {
        jdk "JAVA17"
    }
    stages {
        stage("Notify Discord") {
            steps {
                discordSend webhookURL: env.FDD_WH_ADMIN,
                        title: "Deploy Started: SDLink 1.20 Deploy #${BUILD_NUMBER}",
                        link: env.BUILD_URL,
                        result: 'SUCCESS',
                        description: "Build: [${BUILD_NUMBER}](${env.BUILD_URL})"
            }
        }
        stage("Prepare") {
            steps {
                sh "curl https://raw.githubusercontent.com/hypherionmc/changelogs/main/sdlink/changelog-forge.md --output changelog-forge.md"
                sh "curl https://raw.githubusercontent.com/hypherionmc/changelogs/main/sdlink/changelog-fabric.md --output changelog-fabric.md"
                sh "chmod +x ./gradlew"
                sh "./gradlew clean"
                sh "./gradlew build"
            }
        }
        stage("Publish") {
            steps {
                sh "./gradlew publishMod -Prelease=true"
            }
        }
    }
    post {
        always {
            sh "./gradlew --stop"
            deleteDir()

            discordSend webhookURL: env.FDD_WH_ADMIN,
                    title: "SDLink 1.20 Deploy #${BUILD_NUMBER}",
                    link: env.BUILD_URL,
                    result: currentBuild.currentResult,
                    description: "Build: [${BUILD_NUMBER}](${env.BUILD_URL})\nStatus: ${currentBuild.currentResult}"
        }
    }
}
