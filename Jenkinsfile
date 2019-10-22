def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def home = "/home/jenkins"
def workspace = "${home}/workspace/build-docker-jenkins"
def workdir = "${workspace}/src/localhost/docker-jenkins/"

podTemplate(
        containers: [
                containerTemplate(name: 'jnlp', image: 'florianseidel/capstone-build-slave:latest'),
                containerTemplate(name: 'docker', image: 'docker', command: 'tail -f /dev/null', ttyEnabled: true),
                containerTemplate(name: 'maven', image: 'maven:3.6.2-jdk-11-slim', command: 'tail -f /dev/null', ttyEnabled: true)
            ],
        volumes: [
            hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
            ],
        )
/*podTemplate(yaml: """
apiVersion: v1
kind: Pod
name: test-build-pod
metadata:
  labels:
    some-label: ${label}
spec:
  containers:
    - name: jnlp
      image: florianseidel/capstone-build-slave:latest
      env:
          - name: DOCKER_HOST
            value: tcp://localhost:2375
    - name: dind
      image: docker:18.05-dind
      securityContext:
        privileged: true
      volumeMounts:
        - name: dind-storage
          mountPath: /var/lib/docker
  volumes:
    - name: dind-storage
      emptyDir: {}
"""
)*/ {
    node(POD_LABEL) {
        dir(workdir) {
            stage('Checkout') {
                timeout(time: 3, unit: 'MINUTES') {
                    checkout scm
                }
            }
            stage('Build') {
                echo "Building service..."
                sh "chmod u+x mvnw &&./mvnw package"
            }
        }
    }
}