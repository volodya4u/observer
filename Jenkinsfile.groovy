pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        CLUSTER_NAME = 'observer-cluster'
        SERVICE_NAME = 'observer-service'
    }

    stages {
        stage('Build') {
            steps {
                script {
                    bat 'docker logout public.ecr.aws'
                    bat 'mvn compile dependency:copy-dependencies -DincludeScope=runtime'
                    bat 'docker build --platform linux/amd64 -t observer -f Dockerfile_lambda .'
                }
            }
        }

        stage('Configure AWS') {
            steps {
                withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AWS1', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    bat 'aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID'
                    bat 'aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY'
                    bat 'aws configure set region $AWS_REGION'
                    bat 'aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 211125372735.dkr.ecr.us-east-1.amazonaws.com'
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    bat 'docker tag observer:latest 211125372735.dkr.ecr.us-east-1.amazonaws.com/observer:latest'
                    bat 'docker push 211125372735.dkr.ecr.us-east-1.amazonaws.com/observer:latest'

                    bat 'aws cloudformation deploy --template-file cloudformation.yml --stack-name dev --capabilities CAPABILITY_IAM --parameter-overrides ClusterName=$CLUSTER_NAME ServiceName=$SERVICE_NAME DesiredCount=1'
                }
            }
        }
    }
}
