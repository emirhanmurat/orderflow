pipeline {
    agent any

    environment {
        IMAGE_TAG = "${GIT_COMMIT.take(7)}"
    }

    stages {

        stage('Detect Changes') {
            steps {
                script {

                    def currentCommit = sh(
                        script: 'git rev-parse HEAD',
                        returnStdout: true
                    ).trim()

                    def previousCommit = env.GIT_PREVIOUS_SUCCESSFUL_COMMIT

                    if (!previousCommit) {
                        echo "No previous successful build found."
                        echo "All services will be treated as changed."

                        env.API_CHANGED = 'true'
                        env.WORKER_CHANGED = 'true'
                        env.NOTIFICATION_CHANGED = 'true'

                    } else {

                        echo "Previous successful commit: ${previousCommit}"
                        echo "Current commit: ${currentCommit}"

                        def changedFiles = sh(
                            script: "git diff --name-only ${previousCommit} ${currentCommit}",
                            returnStdout: true
                        ).trim()

                        echo "Changed files:"
                        echo changedFiles

                        env.API_CHANGED = changedFiles.readLines().any {
                            it.startsWith('order-api/')
                        } ? 'true' : 'false'

                        env.WORKER_CHANGED = changedFiles.readLines().any {
                            it.startsWith('order-worker/')
                        } ? 'true' : 'false'

                        env.NOTIFICATION_CHANGED = changedFiles.readLines().any {
                            it.startsWith('notification-service/')
                        } ? 'true' : 'false'
                    }

                    echo "API_CHANGED=${env.API_CHANGED}"
                    echo "WORKER_CHANGED=${env.WORKER_CHANGED}"
                    echo "NOTIFICATION_CHANGED=${env.NOTIFICATION_CHANGED}"
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
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    ),
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
                            sed -i \
                                "s|image: .*order-api:.*|image: $DOCKER_USERNAME/order-api:$IMAGE_TAG|" \
                                k8s/order-api.yaml
                        fi

                        if [ "$WORKER_CHANGED" = "true" ]; then
                            sed -i \
                                "s|image: .*order-worker:.*|image: $DOCKER_USERNAME/order-worker:$IMAGE_TAG|" \
                                k8s/order-worker.yaml
                        fi

                        if [ "$NOTIFICATION_CHANGED" = "true" ]; then
                            sed -i \
                                "s|image: .*notification-service:.*|image: $DOCKER_USERNAME/notification-service:$IMAGE_TAG|" \
                                k8s/notification-service.yaml
                        fi

                        git add k8s/

                        if ! git diff --cached --quiet; then
                            git commit -m "update images to $IMAGE_TAG"

                            git push \
                                https://$GIT_USERNAME:$GIT_PASSWORD@github.com/$GIT_USERNAME/orderflow.git \
                                HEAD:main
                        else
                            echo "No Kubernetes manifest changes."
                        fi
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