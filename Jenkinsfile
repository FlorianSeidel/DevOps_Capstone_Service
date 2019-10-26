def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def home = "/home/jenkins"
def workspace = "${home}/workspace/build-docker-jenkins"
def workdir = "${workspace}/src/localhost/docker-jenkins/"

podTemplate(
        containers: [
                containerTemplate(name: 'jnlp',
                                  image: 'florianseidel/capstone-build-slave:latest',
                                  envVars: [
                                              envVar(key: 'DOCKER_HOST', value: 'tcp://localhost:2375')
                                          ]
                                  )
            ]
)
{
        podTemplate(yaml: """
apiVersion: v1
kind: Pod
name: test-build-pod
metadata:
  labels:
    some-label: ${label}
spec:
  containers:
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
        ){

            node(POD_LABEL) {
                dir(workdir) {
                    stage('Checkout') {
                        timeout(time: 3, unit: 'MINUTES') {
                            checkout scm
                        }
                    }
                    stage('Build') {
                        echo "Building service..."
                        sh "chmod u+x mvnw && ./mvnw package"
                    }
                    stage('Build Docker Image and Push')
                    {
                        echo "Building docker image..."

                        docker.withRegistry('', 'dockerhub') {
                            sh "docker-compose build"
                            sh "docker tag florianseidel/capstone-service:latest florianseidel/capstone-service:${env.BUILD_ID}"
                            sh "docker push florianseidel/capstone-service:latest"
                            sh "docker push florianseidel/capstone-service:${env.BUILD_ID}"
                        }
                    }
                    stage('Lint Helm Chart')
                    {
                        echo "Linting Helm Chart"
                        sh "helm lint src/helm/capstone-service"
                        sh "helm template src/helm/capstone-service"
                    }
                    stage('Publish Helm Chart')
                    {
                        echo "Publish Helm Chart"
                    }
                }
            }
        }
}
