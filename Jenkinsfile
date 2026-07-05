#!/usr/bin/env groovy

pipeline {
    agent any

    tools {
        maven 'maven-3.9'
    }

    environment {
        DOCKER_REPO = 'mujuules01/demo-app'
        GITHUB_REPO = 'java-maven-app-master-EKS'
        GITHUB_REPO_OWNER = 'mjulestek'
        GIT_BRANCH_TO_PUSH = 'jenkins-jobs'
    }

    stages {
        stage('increment version') {
            steps {
                script {
                    echo 'incrementing the version...'

                    sh 'mvn build-helper:parse-version versions:set -DnewVersion=\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.nextIncrementalVersion} versions:commit'

                    env.IMAGE_TAG = sh(script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true).trim()
                    env.DOCKER_IMAGE = "${DOCKER_REPO}:${IMAGE_TAG}"

                    echo "New app version is: ${IMAGE_TAG}"
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
                AWS_ACCESS_KEY_ID = credentials('jenkins_aws_access_key_id')
                AWS_SECRET_ACCESS_KEY = credentials('jenkins_aws_secret_access_key')
                AWS_REGION = 'eu-central-1'
                EKS_CLUSTER_NAME = 'jennifer-demo-cluster'
                APP_NAME = 'java-maven-app'
                AWS_PAGER = ''
            }

            steps {
                script {
                    echo 'deploying docker image...'

                    env.IMAGE_NAME = "${DOCKER_IMAGE}"

                    sh 'aws sts get-caller-identity'
                    sh 'aws eks update-kubeconfig --region $AWS_REGION --name $EKS_CLUSTER_NAME'

                    sh 'kubectl get nodes'

                    sh 'envsubst < kubernetes/deployment.yaml | kubectl apply -f -'
                    sh 'envsubst < kubernetes/service.yaml | kubectl apply -f -'

                    sh 'kubectl rollout status deployment/$APP_NAME --timeout=180s'
                }
            }
        }

        stage('commit version update to git repo') {
            steps {
                script {
                    echo 'commit version update to git repo...'

                    withCredentials([usernamePassword(credentialsId: 'github-credentials', passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')]) {
                        sh 'git config --global user.email "jenkins@example.com"'
                        sh 'git config --global user.name "jenkins"'

                        sh 'git status'
                        sh 'git branch'

                        sh 'git remote set-url origin https://$GITHUB_USER:$GITHUB_TOKEN@github.com/$GITHUB_REPO_OWNER/$GITHUB_REPO.git'
                        sh 'git remote -v'

                        sh 'git add .'
                        sh 'git commit -m "jenkins increment version [skip ci]" || true'
                        sh 'git push origin HEAD:$GIT_BRANCH_TO_PUSH'
                    }
                }
            }
        }
    }
}