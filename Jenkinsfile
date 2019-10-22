def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def home = "/home/jenkins"
def workspace = "${home}/workspace/build-docker-jenkins"
def workdir = "${workspace}/src/localhost/docker-jenkins/"

podTemplate(label: label,
        containers: [
                containerTemplate(name: 'jnlp', image: 'cloudbees/jnlp-slave-with-java-build-tools'),
                containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true)
            ],
        volumes: [
            hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
            ],
        ) {
    node(label) {
        dir(workdir) {
            stage('Checkout') {
                timeout(time: 3, unit: 'MINUTES') {
                    checkout scm
                }
            }

            stage('Build') {
                container('jnlp') {
                    echo "Building service..."
                    sh "./mvnw package"
                }
            }
        }
    }
}