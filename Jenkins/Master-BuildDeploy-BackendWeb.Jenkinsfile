pipeline {
    agent any
    stages {
        stage ('Checkout code') {
            steps {
                git branch: 'master', credentialsId: '3ccd0e84-c917-4b30-b440-016c57545b2d', url: 'git@gitlab.mi.hdm-stuttgart.de:bk095/InteraktiveMedien.git'
            }
        }
        
        stage ('Build images') {
            steps {
                sh 'docker-compose -f docker-compose.prod.yml build'
            }            
        }
        
        stage ('API tests') {
            steps {
                sh 'docker-compose -f docker-compose.backend-testing-api.yml rm -fsv'
                sh 'docker-compose -f docker-compose.backend-testing-api.yml build'
                sh 'docker-compose -f docker-compose.backend-testing-api.yml up --abort-on-container-exit'
                sh 'docker-compose -f docker-compose.backend-testing-api.yml rm -fsv'
            }            
        }
            
        stage ('Store images') {
            steps {
                sh 'docker-compose -f docker-compose.prod.yml push'
            }            
        }
        
        stage ('Deploy images') {
            steps {
                sh 'docker-compose -f docker-compose.prod.yml stop'
                sh 'docker-compose -f docker-compose.prod.yml rm -sf'
                sh 'docker-compose -f docker-compose.prod.yml pull'
                sh 'docker-compose -f docker-compose.prod.yml up -d'
            }            
        }
        
        stage ('Notification'){
            steps {
                slackSend channel: '#jenkins', message: 'Backend and Web on master were released successfully'
            }            
        }
    }
}
