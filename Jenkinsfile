#!/usr/bin/env groovy

pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    tools {
        maven 'maven-3.9'
    }

    environment {
        AWS_ACCOUNT_ID = '596517178096'
        AWS_REGION = 'eu-central-1'
        EKS_CLUSTER_NAME = 'jennifer-demo-cluster'

        ECR_REPO_NAME = 'java-maven-app'
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        ECR_REPO = "${ECR_REGISTRY}/${ECR_REPO_NAME}"

        APP_NAME = 'java-maven-app'
        AWS_PAGER = ''

        GITHUB_REPO = 'java-maven-app-master-EKS'
        GITHUB_REPO_OWNER = 'mjulestek'
        GIT_BRANCH_TO_PUSH = 'jenkins-jobs'
    }

    stages {
        stage('check skip ci') {
            steps {
                script {
                    echo 'checking commit message...'

                    def commitMessage = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()

                    echo "Last commit message is: ${commitMessage}"

                    if (commitMessage.contains('[skip ci]')) {
                        echo 'Commit contains [skip ci]. Stopping pipeline to avoid loop.'
                        currentBuild.result = 'SUCCESS'
                        error('Stopping pipeline because this is a Jenkins version commit.')
                    }

                    echo 'No [skip ci] found. Continuing pipeline.'
                }
            }
        }

        stage('increment version') {
            steps {
                script {
                    echo 'incrementing the version...'

                    sh 'mvn build-helper:parse-version versions:set -DnewVersion=\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.nextIncrementalVersion} versions:commit'

                    env.IMAGE_TAG = sh(script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true).trim()
                    env.IMAGE_NAME = "${ECR_REPO}:${IMAGE_TAG}"

                    echo "New app version is: ${IMAGE_TAG}"
                    echo "Docker image will be: ${IMAGE_NAME}"
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

        stage('build and push image to ECR') {
            environment {
                AWS_ACCESS_KEY_ID = credentials('jenkins-aws-access-key-id')
                AWS_SECRET_ACCESS_KEY = credentials('jenkins-aws-secret-access-key')
            }

            steps {
                script {
                    echo 'building docker image and pushing to AWS ECR...'

                    sh 'aws sts get-caller-identity'
                    sh 'aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY'

                    sh 'docker build -t $IMAGE_NAME .'
                    sh 'docker push $IMAGE_NAME'
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
                    echo 'deploying docker image to EKS...'

                    sh 'aws sts get-caller-identity'
                    sh 'aws eks update-kubeconfig --region $AWS_REGION --name $EKS_CLUSTER_NAME'

                    sh 'kubectl get nodes'

                    sh 'envsubst < kubernetes/deployment.yaml | kubectl apply -f -'
                    sh 'envsubst < kubernetes/service.yaml | kubectl apply -f -'

                    sh 'kubectl rollout status deployment/$APP_NAME --timeout=180s'
                }
            }
        }

        stage('commit version update') {
            steps {
                script {
                    echo 'committing version update to GitHub repository...'

                    withCredentials([usernamePassword(credentialsId: 'github-credentials', passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')]) {
                        sh 'git config --global user.email "jenkins@example.com"'
                        sh 'git config --global user.name "jenkins"'

                        sh 'git status'
                        sh 'git branch'

                        sh 'git remote set-url origin https://$GITHUB_USER:$GITHUB_TOKEN@github.com/$GITHUB_REPO_OWNER/$GITHUB_REPO.git'
                        sh 'git remote -v'

                        sh 'git add pom.xml'
                        sh 'git commit -m "jenkins increment version [skip ci]" || true'
                        sh 'git pull --rebase origin $GIT_BRANCH_TO_PUSH'
                        sh 'git push origin HEAD:$GIT_BRANCH_TO_PUSH'
                    }
                }
            }
        }
    }
}