pipeline {
    agent any

    parameters {
        choice(name: 'VERSION', choices: ['1.1.0', '1.2.0', '1.3.0'], description: '')
        booleanParam(name: 'executeTests', defaultValue: true, description: '')
    }

    stages {
        stage("init") {
            steps {
                script {
                    gv = load "script.groovy"
                }
            }
        }
        stage("build") {
            steps {
                script{
                    gv.buildApplication()
                }  
            }    
        }

        stage("test") {
            when {
                expression {
                    params.executeTests
                }
            }
            steps {
                script{
                    gv.testApplication()
                }
            }
        }

        stage("deploy") {
            steps {
                script{
                    gv.deployApplication()
                }
            }
        }
    }
}
