
node('node') {
    currentBuild.result = "SUCCESS"

    try {

       stage('Checkout'){
          checkout scm
       }

       stage('Build'){
		echo "Build service"
       }

       stage('Build Docker'){
         echo 'Build docker'
       }

       stage('Deploy'){
         echo 'Push to Repo'
       }

       stage('Cleanup'){

         echo 'Cleanup'

         mail body: 'Project build successful',
                     from: 'jenkins@DevOps_Capstone.info',
                     replyTo: 'jenkins@DevOps_Capstone.info',
                     subject: 'SUCCESS',
                     to: 'seidel.florian@gmail.com'
       }
    }
    catch (err) {

        currentBuild.result = "FAILURE"

        mail body: 'Project build failure',
                    from: 'jenkins@DevOps_Capstone.info',
                    replyTo: 'jenkins@DevOps_Capstone.info',
                    subject: 'FAILURE',
                    to: 'seidel.florian@gmail.com'

        throw err
    }

}