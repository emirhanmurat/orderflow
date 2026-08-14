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
        stage('Update Kubernetes Manifests') {
            steps {
                withCredentials([
                usernamePassword(
                credentialsId: '66c5ef41-2b3c-4fd2-a4d5-5cd3ddb716f3',
                usernameVariable: 'GIT_USERNAME',
                passwordVariable: 'GIT_PASSWORD'
            )
        ]) {
                    sh '''
                git config user.name "jenkins"
                git config user.email "jenkins@localhost"

                if [ "$API_CHANGED" = "true" ]; then
                    sed -i "s|image: .*order-api:.*|image: $DOCKER_USERNAME/order-api:$IMAGE_TAG|" k8s/order-api.yaml
                fi

                if [ "$WORKER_CHANGED" = "true" ]; then
                    sed -i "s|image: .*order-worker:.*|image: $DOCKER_USERNAME/order-worker:$IMAGE_TAG|" k8s/order-worker.yaml
                fi

                if [ "$NOTIFICATION_CHANGED" = "true" ]; then
                    sed -i "s|image: .*notification-service:.*|image: $DOCKER_USERNAME/notification-service:$IMAGE_TAG|" k8s/notification-service.yaml
                fi

                git add k8s/

                git diff --cached --quiet || \
                git commit -m "update images to $IMAGE_TAG"

                git push https://$GIT_USERNAME:$GIT_PASSWORD@github.com/$GIT_USERNAME/orderflow.git HEAD:main
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
