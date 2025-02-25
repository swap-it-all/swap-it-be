pipeline {
    agent any
    stages {
        stage('GitHub Repository Clone') {
            steps {
                git branch: 'develop', credentialsId: 'github-token', url: 'https://github.com/swap-it-all/swap-it-be'
            }
        }
        stage('SwapIt Service Gradle Project Test') {
            steps {
                sh './gradlew clean test -Dspring.profiles.active=test'
            }
        }
        stage('SwapIt Service Gradle Project Build') {
            steps {
                sh './gradlew clean bootJar'
            }
        }
        stage('SwapIt Service Project Deploy') {
            steps {
                sshagent(credentials: ['aws-ssh-key']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no $AWS_IP_ADDRESS uptime
                        scp /var/jenkins_home/workspace/swapit-pipeline/build/libs/swapit-0.0.1-SNAPSHOT.jar $AWS_IP_ADDRESS:/home/ubuntu/swap-it-be/build/libs
                        ssh -T $AWS_IP_ADDRESS "export DB_URL=$DB_URL && \
                        export DB_USERNAME=$DB_USERNAME && \
                        export DB_PASSWORD=$DB_PASSWORD && \
                        export ACCESS_SECRET_KEY=$ACCESS_SECRET_KEY && \
                        export REFRESH_SECRET_KEY=$REFRESH_SECRET_KEY && \
                        export ACCESS_TOKEN_VALID_TIME=$ACCESS_TOKEN_VALID_TIME && \
                        export REFRESH_TOKEN_VALID_TIME=$REFRESH_TOKEN_VALID_TIME && \
                        export MAIL_USER_NAME=$MAIL_USER_NAME && \
                        export MAIL_PASSWORD=$MAIL_PASSWORD && \
                        export MAIL_SEND_TO=$MAIL_SEND_TO && \
                        export S3_BUCKET_NAME=$S3_BUCKET_NAME && \
                        bash ./deploy.sh"
                    '''
                }
            }
        }
    }
}