pipeline {
    agent {
        docker { 
            image 'toncorp/android-builder:1.0.2' 
            args '-v $HOME/.gradle:/root/.gradle' 
        }
    }
    environment {
        ANDROID_HOME = "/usr/local/android-sdk-linux"
        //S3
        S3_BUCKET = "tongram"
        AWS_REGION = "ap-southeast-1"
        S3_PATH = "AppBuild/android-releases" 
    }

    stages {
        stage('Build Android') {
            steps {
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
                    sh """
                        echo "Building Release APK and AAB..."
                    """
                    // sh """
                    //     ./gradlew assembleRelease bundleRelease \
                    //     -Pandroid.injected.signing.store.file='$KEYSTORE_FILE' \
                    //     -Pandroid.injected.signing.store.password='$STORE_PASS' \
                    //     -Pandroid.injected.signing.key.alias='$KEY_ALIAS' \
                    //     -Pandroid.injected.signing.key.password='$KEY_PASS'
                    // """
                }
            }
        }
        stage('Upload to S3') {
            steps{
                def apkPath = sh(script: 'find . -name "**/build/outputs/apk/afat/*.apk" | head -n 1', returnStdout: true).trim()
                def aabPath = sh(script: 'find . -name "**/build/outputs/bundle/afatRelease/*.aab" | head -n 1', returnStdout: true).trim()
                def timeStamp = new Date().format("yyyyMMdd-HHmm")
                withAWS(region: 'ap-southeast-1', credentials: "${AWS_CREDENTIALS_ID}") {
                    s3Upload acl: 'PublicRead', bucket: 'tongram', file: "/${apkPath}", path: "AppBuild/${timeStamp}/"
                    s3Upload acl: 'PublicRead', bucket: 'tongram', file: "/${aabPath}", path: "AppBuild/${timeStamp}/"
                }
            }`
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '**/build/outputs/apk/**/*.apk, **/build/outputs/bundle/**/*.aab', fingerprint: true
        }
    }
}