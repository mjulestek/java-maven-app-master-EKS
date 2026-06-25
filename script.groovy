def buildApplication() {
    echo 'building the application...'
}
return this

def testApplication() {
    echo 'testing the application...'
}
return this

def deployApplication() {
    echo 'deploying the application...'
    echo "deploying version ${params.VERSION}"
}
return this
