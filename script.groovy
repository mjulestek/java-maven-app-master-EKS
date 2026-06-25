def buildJar() {
    echo 'Packaging the jar file...'
    sh 'mvn package'
}
return this

def buildApplication() {
    echo "building the docker image..."
    withCredentials ([usernamePassword(credentialsId: 'docker-hub-repo', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh "docker build -t mujuules01/demo-app:jma-2.0 ."
        sh "echo $PASS | docker login -u $USER --password-stdin"
        sh 'docker push mujuules01/demo-app:jma-2.0'
}                
return this

def deployApplication() {
    echo 'deploying the application...'
    }
return this
