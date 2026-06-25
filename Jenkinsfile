def gv

pipeline {   
    agent any
    tools {
        maven 'maven-3.9'
    }
    stages {
        
        stage("build jar") {
            steps {
                script {
                    echo "packaging the application..."
                    sh 'mvn package'

                }
            }
        }

        stage("build image") {
            steps {
                script {
                    echo "building the docker image..."
                    withCredentials ([usernamePassword(credentialsId: 'docker-hub-repo', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                        sh "docker build -t mujuules01/demo-app:jma-2.0 ."
                        sh "echo $PASS | docker login -u $USER --password-stdin"
                        sh 'docker push mujuules01/demo-app:jma-2.0'
                    }
                }
            }
        }

        stage("deploy") {
            steps {
                script {
                    gv.deployApp()
                }
            }
        }               
    }
} 
