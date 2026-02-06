pipeline {
    agent {
        docker { 
            image 'toncorp/android-builder:1.0.2' 
            alwaysPull true 
            args '-v $HOME/.gradle:/root/.gradle' 
        }
    }
    environment {
        ANDROID_HOME = "/usr/local/android-sdk-linux"
        _JAVA_OPTIONS = "-Xmx4096m"
        GRADLE_OPTS = "-Dorg.gradle.jvmargs=-Xmx4096m"
        //S3
        S3_BUCKET = "tongram"
        AWS_REGION = "ap-southeast-1"
        S3_PATH = "AppBuild/android-releases" 
        // Telegram configuration
        TOKEN = credentials('b4a49b21-4caa-4f7a-834b-ffa7d6b9c41e')
        CHAT_ID = credentials('69503db3-8106-40c6-8bd0-876b2eb2adb7')
        TON_DEV_API_URL = 'https://tongram-ai-dev-v2.motcaigido.xyz/ton-api/api/v1/'
        TON_PROD_API_URL = 'https://tongramai-services.tongram.app/ton-api/api/v1/'
        TON_DEV_API_KEY = '393a7937f6002ced4f9ad22d9421d3db'
        TON_PROD_API_KEY = '7aff3c66fd29e56901e731e7ab7cfc6d'
    }

    stages {
        stage('Notification') {
            steps {
                sh "make notify_start JOB_NAME=${env.JOB_NAME} BUILD_NUMBER=${env.BUILD_NUMBER} CHAT_ID=${env.CHAT_ID} TOKEN=${env.TOKEN}"
            }
        }
        stage('Build Android') {
            steps{
                script {
                    // 1. Checkout Code
                    checkout scm

                    // 2. Setup Permission
                    sh 'chmod +x gradlew'

                    // 3. Check Info (Optional)
                    sh 'java -version'

                    // 4. Build Release APK and AAB
                    withCredentials([
                        file(credentialsId: 'android-keystore-file', variable: 'KEYSTORE_FILE'),
                        string(credentialsId: 'STORE_PASSWORD', variable: 'STORE_PASS'),
                        string(credentialsId: 'KEY_ALIAS', variable: 'KEY_ALIAS'),
                        string(credentialsId: 'KEY_PASSWORD', variable: 'KEY_PASS')
                    ]) {
                        switch(env.BRANCH_NAME) {
                            case "develop":
                                sh """
                                    echo "Building Debug APK"

                                    ./gradlew assembleDebug \
                                    -PtonDevAPIUrl='${env.TON_DEV_API_URL}' \
                                    -PtonDevAPIKey='${env.TON_DEV_API_KEY}' \
                                    -Pandroid.injected.signing.store.file='$KEYSTORE_FILE' \
                                    -Pandroid.injected.signing.store.password='$STORE_PASS' \
                                    -Pandroid.injected.signing.key.alias='$KEY_ALIAS' \
                                    -Pandroid.injected.signing.key.password='$KEY_PASS'
                                """
                                break
                            case "production":
                                sh """
                                    echo "Building Release APK"

                                    ./gradlew assembleRelease \
                                    -PtonProdAPIUrl='${env.TON_PROD_API_URL}' \
                                    -PtonProdAPIKey='${env.TON_PROD_API_KEY}' \
                                    -Pandroid.injected.signing.store.file='$KEYSTORE_FILE' \
                                    -Pandroid.injected.signing.store.password='$STORE_PASS' \
                                    -Pandroid.injected.signing.key.alias='$KEY_ALIAS' \
                                    -Pandroid.injected.signing.key.password='$KEY_PASS'
                                """
                                break
                            default:
                                error "Branch ${env.BRANCH_NAME} is not allowed to build."
                        }
                    }
                }
            }
        }
        stage('Build Release AAB') {
            when {
                anyOf {
                    branch 'production'
                }
            }
            steps{
                withCredentials([
                    file(credentialsId: 'android-keystore-file', variable: 'KEYSTORE_FILE'),
                    string(credentialsId: 'STORE_PASSWORD', variable: 'STORE_PASS'),
                    string(credentialsId: 'KEY_ALIAS', variable: 'KEY_ALIAS'),
                    string(credentialsId: 'KEY_PASSWORD', variable: 'KEY_PASS')
                ]) {
                    sh """
                        echo "Building Release AAB"

                        ./gradlew bundleRelease \
                        -PtonProdAPIUrl='${env.TON_PROD_API_URL}' \
                        -PtonProdAPIKey='${env.TON_PROD_API_KEY}' \
                        -Pandroid.injected.signing.store.file='$KEYSTORE_FILE' \
                        -Pandroid.injected.signing.store.password='$STORE_PASS' \
                        -Pandroid.injected.signing.key.alias='$KEY_ALIAS' \
                        -Pandroid.injected.signing.key.password='$KEY_PASS'
                    """
                }
            }

        }
        stage('Upload apk to S3') {
            steps {
                script {
                    switch(env.BRANCH_NAME) {
                        case "develop":
                            withAWS(region: 'ap-southeast-1', credentials: "AWS_CREDENTIALS_ID") {
                                s3Upload acl: 'PublicRead', bucket: 'tongram', file: "TMessagesProj_App/build/outputs/apk/afat/debug/app.apk", path: "${S3_PATH}/${env.BRANCH_NAME}/${env.BUILD_NUMBER}/"
                            }
                            break
                        case "production":
                            withAWS(region: 'ap-southeast-1', credentials: "AWS_CREDENTIALS_ID") {
                                s3Upload acl: 'PublicRead', bucket: 'tongram', file: "TMessagesProj_App/build/outputs/apk/afat/release/app.apk", path: "${S3_PATH}/${env.BRANCH_NAME}/${env.BUILD_NUMBER}/"
                                s3Upload acl: 'PublicRead', bucket: 'tongram', file: "TMessagesProj_App/build/outputs/bundle/afatRelease/TMessagesProj_App-afat-release.aab", path: "${S3_PATH}/${env.BRANCH_NAME}/${env.BUILD_NUMBER}/"
                            }
                            break
                        default:
                            error "Branch ${env.BRANCH_NAME} is not allowed to upload."
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                def status = currentBuild.result ?: 'SUCCESS'
                sh """
                make notify_${status.toLowerCase()} JOB_NAME=${env.JOB_NAME} BUILD_NUMBER=${env.BUILD_NUMBER} CHAT_ID=${env.CHAT_ID} TOKEN=${env.TOKEN} APK_LINK=https://${S3_BUCKET}.s3.${AWS_REGION}.amazonaws.com/${S3_PATH}/${env.BRANCH_NAME}/${env.BUILD_NUMBER}/app.apk AAB_LINK=https://${S3_BUCKET}.s3.${AWS_REGION}.amazonaws.com/${S3_PATH}/${env.BRANCH_NAME}/${env.BUILD_NUMBER}/TMessagesProj_App-afat-release.aab
                """
            }
        }
        
    }
}