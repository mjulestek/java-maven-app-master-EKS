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
                    gv.buildJar()

                }
            }
        }

        stage("build image") {
            steps {
                script {
                    gv.buildApplication()
                    }
                }
            }
        }

        stage("deploy") {
            steps {
                script {
                    gv.deployApplication()
                }
            }
        }               
    }
} 
