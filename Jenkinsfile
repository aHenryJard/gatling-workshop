pipeline {
    agent any
    stages {
        stage('Run gatling on CasC Bundle API') {
            steps {
                echo 'Are you ready ??'
                sh '''
                mvn gatling:test
                '''
                gatlingArchive()
            }
        }

        stage('Archiving') {
            steps {
                gatlingArchive()
            }
        }
    }
}