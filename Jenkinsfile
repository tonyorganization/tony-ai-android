pipeline {
    agent {
        docker { 
            image 'toncorp/android-builder:1.0.0' 
            // registryCredentialsId 'docker-registry-creds'
            // registryUrl 'https://hub.playgroundvina.com'
            args '-v $HOME/.gradle:/root/.gradle' 
        }
    }

    environment {
        ANDROID_HOME = "/usr/local/android-sdk-linux"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Setup Permission') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        // stage('Unit Test') {
        //     steps {
        //         sh './gradlew testDebugUnitTest'
        //     }
        // }

        stage('Check Info') {
            steps {
                sh 'java -version'
                sh 'ls $ANDROID_HOME'
            }
        }

        stage('Build Release') {
            steps {
                withCredentials([
                    file(credentialsId: 'android-keystore-file', variable: 'KEYSTORE_FILE'),
                    string(credentialsId: 'STORE_PASSWORD', variable: 'STORE_PASS'),
                    string(credentialsId: 'KEY_ALIAS', variable: 'KEY_ALIAS'),
                    string(credentialsId: 'KEY_PASSWORD', variable: 'KEY_PASS')
                ]) {
                    sh """
                        ./gradlew assembleRelease \
                        -Pandroid.injected.signing.store.file='$KEYSTORE_FILE' \
                        -Pandroid.injected.signing.store.password='$STORE_PASS' \
                        -Pandroid.injected.signing.key.alias='$KEY_ALIAS' \
                        -Pandroid.injected.signing.key.password='$KEY_PASS'
                    """
                }
            }
        }
    }
}