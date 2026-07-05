#!/usr/bin/env groovy

pipeline {
    agent any

    tools {
        maven 'maven-3.9'
    }

    environment {
        DOCKER_REPO = 'mujuules01/demo-app'
        APP_NAME = 'java-maven-app'

        AWS_REGION = 'eu-central-1'
        EKS_CLUSTER_NAME = 'jennifer-demo-cluster'
        AWS_PAGER = ''
    }

    stages {
        stage('increment version') {
            steps {
                script {
                    echo 'incrementing the version...'

                    sh 'mvn build-helper:parse-version versions:set -DnewVersion=\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.nextIncrementalVersion} versions:commit'

                    env.APP_VERSION = sh(script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true).trim()
                    env.IMAGE_TAG = "${APP_VERSION}-${BUILD_NUMBER}"
                    env.DOCKER_IMAGE = "${DOCKER_REPO}:${IMAGE_TAG}"
                    env.IMAGE_NAME = "${DOCKER_IMAGE}"

                    echo "New app version is: ${APP_VERSION}"
                    echo "Docker image tag is: ${IMAGE_TAG}"
                    echo "Docker image will be: ${DOCKER_IMAGE}"
                }
            }
        }

        stage('build app') {
            steps {
                script {
                    echo 'building the application...'
                    sh 'mvn clean package'
                }
            }
        }

        stage('build image') {
            steps {
                script {
                    echo 'building the docker image...'

                    withCredentials([usernamePassword(credentialsId: 'docker-hub-repo', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
                        sh 'docker build -t $DOCKER_IMAGE .'
                        sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                        sh 'docker push $DOCKER_IMAGE'
                    }
                }
            }
        }

        stage('deploy') {
            environment {
                AWS_ACCESS_KEY_ID = credentials('jenkins-aws-access-key-id')
                AWS_SECRET_ACCESS_KEY = credentials('jenkins-aws-secret-access-key')
            }

            steps {
                script {
                    echo 'deploying docker image...'

                    sh 'aws sts get-caller-identity'
                    sh 'aws eks update-kubeconfig --region $AWS_REGION --name $EKS_CLUSTER_NAME'

                    sh 'kubectl get nodes'

                    sh 'envsubst < kubernetes/deployment.yaml | kubectl apply -f -'
                    sh 'envsubst < kubernetes/service.yaml | kubectl apply -f -'

                    sh 'kubectl rollout status deployment/$APP_NAME --timeout=180s'
                }
            }
        }
    }
}