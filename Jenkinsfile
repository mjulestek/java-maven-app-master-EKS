#!/usr/bin/env groovy

pipeline {
    agent any

    tools {
        maven 'maven-3.9'
    }

    stages {
        stage('increment version') {
            steps {
                script {
                    echo 'incrementing the version...'
                    sh 'mvn build-helper:parse-version versions:set -DnewVersion=\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.nextIncrementalVersion} versions:commit'
                }
            }
        }

        stage('build app') {
            steps {
                script {
                    echo 'building the application...'
                    sh 'mvn package'
                }
            }
        }

        stage('build image') {
            steps {
                script {
                    echo 'building the docker image...'
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-repo', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                        sh 'docker build -t mujuules01/demo-app:jma-4.0 .'
                        sh 'echo $PASS | docker login -u $USER --password-stdin'
                        sh 'docker push mujuules01/demo-app:jma-4.0'
                    }
                }
            }
        }

        stage('deploy') {
            steps {
                script {
                    echo 'deploying the application...'
                }
            }
        }
    }
}