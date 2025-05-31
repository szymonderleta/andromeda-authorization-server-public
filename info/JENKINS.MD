```bash

pipeline {

agent any

    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9.9'
    }

    environment {
        TOMCAT_URL = 'https://milkyway.local:8555/manager/text' // Tomcat URL 
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master',
                credentialsId: 'andromeda-auth-id',
                url: 'git@github.com:szymonderleta/andromeda-authorization-server-public.git'
            }
        }
        
        stage('Verify Branch') {
            steps {
                script {
                    def branchName = sh(
                        script: 'git rev-parse --abbrev-ref HEAD',
                        returnStdout: true
                    ).trim()

                    if (branchName != "master") {
                        error("The build is only triggered for the `master` branch. Current branch: ${branchName}.")
                    }
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install' // Build an test
            }
        }
        
        stage('Rename WAR File') {
            steps {
                script {
                    // Dowloading Maven version
                    def version = sh(
                        script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
                        returnStdout: true
                    ).trim()

                    // Delete "-SNAPSHOT" from version, if exist
                    def formattedVersion = version.replace("-SNAPSHOT", "")

                    // Dynamic change WAR file name
                    def warFileOriginal = "target/andromeda-authorization-server-${version}.war"
                    def warFileRenamed = "target/andromeda-authorization-server##${formattedVersion}.war"

                    // Creating file and changin name
                    if (!fileExists(warFileOriginal)) {
                        error "Not found file: ${warFileOriginal}"
                    }

                    sh "mv ${warFileOriginal} ${warFileRenamed}"

                    // Storing filename fot next steps
                    env.RENAMED_WAR_FILE = warFileRenamed
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                script {
                    // Getting Credentials and deliviery to Tomcata
                    withCredentials([usernamePassword(credentialsId: 'tomcat-credentials', usernameVariable: 'TOMCAT_USER', passwordVariable: 'TOMCAT_PASS')]) {
                        def result = sh(
                            script: """
                            curl -u $TOMCAT_USER:$TOMCAT_PASS -T $RENAMED_WAR_FILE \
                            "$TOMCAT_URL/deploy?path=/authorization-server&update=true"
                            """,
                            returnStatus: true
                        )
                        if (result != 0) {
                            error "Deploying on Tomcat failed! Output error: ${result}"
                        } else {
                            echo "App was deployed with success."
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs() // Cleaning workspace
        }
        success {
            echo 'Deployment succeeded!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
```
