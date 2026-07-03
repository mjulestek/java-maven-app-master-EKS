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
        stage('commit version update to git repo') {
            steps {
                script {
                    echo 'commit version update to git repo...'
                    withCredentials([usernamePassword(credentialsId: 'github-credentials', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                        sh 'git config --global user.email "mjules.tek@gmail.com"'
                        sh 'git config --global user.name "mjules.tek"'

                        sh 'git status'
                        sh 'git branch'
                        sh 'git config --list'
                        sh 'git remote set-url origin https://$USER:$PASS@github.com/$USER/java-maven-app-master-EKS.git'
                        sh 'git add .'
                        sh 'git commit -m "jenkins increment version"'
                        sh 'git push origin HEAD:jenkins-jobs'  
                    }
                }
            }
        }

    }
}