pipeline {
    agent any

    environment {
        IMAGE_TAG = "${GIT_COMMIT.take(7)}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    env.API_CHANGED = sh(
                        script: "git diff --name-only HEAD~1 HEAD | grep -q '^order-api/'",
                        returnStatus: true
                    ) == 0 ? 'true' : 'false'

                    env.WORKER_CHANGED = sh(
                        script: "git diff --name-only HEAD~1 HEAD | grep -q '^order-worker/'",
                        returnStatus: true
                    ) == 0 ? 'true' : 'false'

                    env.NOTIFICATION_CHANGED = sh(
                        script: "git diff --name-only HEAD~1 HEAD | grep -q '^notification-service/'",
                        returnStatus: true
                    ) == 0 ? 'true' : 'false'
                }
            }
        }

        stage('Docker Login') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" |
                        docker login \
                          -u "$DOCKER_USERNAME" \
                          --password-stdin
                    '''
                }
            }
        }

        stage('Build & Push order-api') {
            when {
                environment name: 'API_CHANGED', value: 'true'
            }

            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        docker build \
                          -t "$DOCKER_USERNAME/order-api:$IMAGE_TAG" \
                          ./order-api

                        docker push \
                          "$DOCKER_USERNAME/order-api:$IMAGE_TAG"
                    '''
                }
            }
        }

        stage('Build & Push order-worker') {
            when {
                environment name: 'WORKER_CHANGED', value: 'true'
            }

            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        docker build \
                          -t "$DOCKER_USERNAME/order-worker:$IMAGE_TAG" \
                          ./order-worker

                        docker push \
                          "$DOCKER_USERNAME/order-worker:$IMAGE_TAG"
                    '''
                }
            }
        }

        stage('Build & Push notification-service') {
            when {
                environment name: 'NOTIFICATION_CHANGED', value: 'true'
            }

            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        docker build \
                          -t "$DOCKER_USERNAME/notification-service:$IMAGE_TAG" \
                          ./notification-service

                        docker push \
                          "$DOCKER_USERNAME/notification-service:$IMAGE_TAG"
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
    }
}