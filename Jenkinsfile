pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh './gradlew clean test -Dspring.profiles.active=test'
            }
        }
        stage('Build') {
            steps {
                sh './gradlew clean bootJar'
            }
        }
        stage('Deploy') {
            when {
                branch 'develop'
            }
            steps {
                sshagent(credentials: ['aws-ssh-key']) {
                    sh '''
                        export FIREBASE_CONFIG_B64=$(echo "$FIREBASE_CONFIG" | base64 -w 0)
                        ssh -o StrictHostKeyChecking=no $AWS_IP_ADDRESS uptime
                        scp $JENKINS_ROUTE $AWS_IP_ADDRESS:$AWS_ROUTE
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
                        export REDIS_HOST=$REDIS_HOST && \
                        export FIREBASE_CONFIG=$(echo $FIREBASE_CONFIG_B64 | base64 -d) && \
                        bash ./deploy.sh"
                    '''
                }
            }
        }
    }
}
