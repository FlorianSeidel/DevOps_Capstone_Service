def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def home = "/home/jenkins"
def workspace = "${home}/workspace/build-docker-jenkins"
def workdir = "${workspace}/src/localhost/docker-jenkins/"
def helmRepo = "DevOps_Capstone_Repo"

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
                    state("Check pre-conditions")
                    {
                        def pom = readMavenPom file: pom.xml
                        if(!(env.BRANCH_NAME.startsWith("release/")
                             || env.BRANCH_NAME == "master"
                             || env.BRANCH_NAME.startsWith("feature/")))
                        {
                            error("Only release/*, feature/* and master branches allowed.")
                        }
                        else if (env.BRANCH_NAME.startsWith("release/"))
                        {
                            if (pom.version.endsWith("-SNAPSHOT"))
                            {
								error("SNAPSHOT versions not allowed on release branches.")
                            }
                        }
                    }
                    stage("Lint docker files")
                    {
						sh "docker run --rm -i hadolint/hadolint < src/main/docker/Dockerfile.jvm"
						sh "docker run --rm -i hadolint/hadolint < src/main/docker/Dockerfile.native"
                    }
                    if(env.BRANCH_NAME.startsWith("release/"))
                    {
                        stage('Modify Helm Chart for release')
                        {
                            def pom = readMavenPom file: 'pom.xml'
                            def valuesFile = 'src/helm/capstone-service/values.yaml'
                            def valuesData = readYaml file: valuesFile
                            valuesData.image.tag = pom.version
                            sh "rm ${valuesFile}"
                            writeYaml file: valuesFile, data: valuesData
                            sh "cat ${valuesFile}"

                            def chartFile = 'src/helm/capstone-service/Chart.yaml'
                            def chartData = readYaml file: chartFile
                            chartData.version = pom.version
                            chartData.appVersion = pom.version
                            sh "rm ${chartFile}"
                            writeYaml file: chartFile, data: chartData
                            sh "cat ${chartFile}"
                        }
                    }
                    stage('Lint Helm Chart')
                    {
                        echo "Linting Helm Chart"
                        sh "helm lint src/helm/capstone-service"
                        sh "helm template src/helm/capstone-service"
                    }
                    stage('Build') {
                        echo "Building service..."
                        sh "chmod +x mvnw && ./mvnw package"
                    }
                    stage('Build Docker Image and Push')
                    {
                        echo "Building docker image..."
                        docker.withRegistry('', 'dockerhub') {

	                        sh "docker-compose build"
							if(env.BRANCH_NAME == "master")
							{
	                            sh "docker push florianseidel/capstone-service:latest"
		                    }
		                    else if(env.BRANCH_NAME.startsWith("feature/"))
		                    {
		                        def featureTag = env.BRANCH_NAME.split("/")[1]
		                        sh "docker tag florianseidel/capstone-service:latest florianseidel/capstone-service:${featureTag}"
                                sh "docker push florianseidel/capstone-service:${featureTag}"
		                    }
		                    else if(env.BRANCH_NAME.startsWith("release/"))
		                    {
		                        //check if image tag exists remotely
			                    def pom = readMavenPom file: 'pom.xml'
		                        def exists = sh(returnStatus:true, script: "curl --silent -f -lSL https://index.docker.io/v1/repositories/florianseidel/capstone-service/tags/${pom.version} > /dev/null")
		                        if (!exists)
		                        {
			                        sh "docker tag florianseidel/capstone-service:latest florianseidel/capstone-service:${pom.version}"
			                        sh "docker push florianseidel/capstone-service:${pom.version}"
			                        withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
				                        sh 'git tag -a service-v${pom.version} -m "Published service version ${pom.version}"'
	                                    sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/FlorianSeidel/DevOps_Capstone_Service.git --tags"
                                    }
			                    }
			                    else
			                    {
			                        error("Image will not be built and pushed, because an image with the same tag ${pom.version} already exists in the registry.")
			                    }
		                    }
	                    }
                    }
                    if(env.BRANCH_NAME.startsWith("release/"))
                    {
	                    stage('Publish Helm Chart')
	                    {
	                        echo "Publish Helm Chart"
	                        sh "mkdir .deploy"
	                        sh "helm package src/helm/capstone-service --destination .deploy"
	                        // This will fail the build if a chart with the same tag already exists in the repo.
	                        sh "cr upload -o FlorianSeidel -r ${helmRepo} -p .deploy"

                            //Tag helm chart release
                            def versionLine = sh(returnStdout:true, script: "helm inspect ./src/helm/capstone-service | grep ^version:")
                            def helmVersion = versionLine.split()[1]
                            withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {

                                sh "git clone https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/FlorianSeidel/${helmRepo}.git repo"
								dir("repo")
								{
		                            sh "cr index -i ./index.yaml -o FlorianSeidel -c https://github.com/FlorianSeidel/DevOps_Capstone_Service.git -r ${helmRepo} -p ../.deploy"
									sh "git commit -m \"Deploy version ${helmVersion} of capstone-service Helm Chart.\""
									sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/FlorianSeidel/${helmRepo}.git"
								}
								//After index was updated successfully, tag the branch and push the image.
								sh 'git tag -a chart-v${helmVersion} -m "Published Helm Chart version ${helmVersion}"'
                                sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/FlorianSeidel/DevOps_Capstone_Service.git --tags"
							}
	                    }
	                }
                }
            }
        }
}
