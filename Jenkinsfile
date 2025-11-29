pipeline{
    agent any
    triggers{
        githubPush()
    }
    environment{
        EMAIL_ID = "siddhesh17122004@gmail.com"
    }
    stages{
        stage('Checkout'){
            steps{
                git branch:'main',url:'https://github.com/Abhinav-Kumar012/lastmile_microservice.git'
            }
        }
        stage('Build'){
            steps{
                dir('backend-services'){
                    sh './mvnw clean install -DskipTests'
                }
            }
        }
        stage('Build Docker Images'){
            steps{
                dir('backend-services'){
                    sh 'docker compose build'
                }
            }
        }
        stage('Push Docker Images to dockerHub') {
            steps {
                dir('backend-services'){
                    script {
                        docker.withRegistry('https://index.docker.io/v1/', 'DockerHubCred') {
                            sh 'docker compose push'
                        }
                    }
                }
            }
        }       
        stage('Ansible Deployment'){
            steps{
                dir('ansible'){
                    sh 'ansible-playbook -i inventory.ini deploy.yml'
                }
            }
        }
    }
    post {
        success {
            mail to: "${EMAIL_ID}",
                 subject: "Jenkins Pipeline Succeeded: ${currentBuild.fullDisplayName}",
                 body: "Good news! The pipeline ${env.JOB_NAME} build #${env.BUILD_NUMBER} succeeded.\nCheck it here: ${env.BUILD_URL}"
        }
        failure {
            mail to: "${EMAIL_ID}",
                 subject: "Jenkins Pipeline Failed: ${currentBuild.fullDisplayName}",
                 body: "Attention! The pipeline ${env.JOB_NAME} build #${env.BUILD_NUMBER} failed.\nCheck details here: ${env.BUILD_URL}"
        }
//         cleanup{
//             cleanWs()
//         }
    }
}