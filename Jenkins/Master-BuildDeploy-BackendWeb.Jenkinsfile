node {
    stage ('Checkout code') {
        git brach: 'master', credentialsId: '3ccd0e84-c917-4b30-b440-016c57545b2d', url: 'git@gitlab.mi.hdm-stuttgart.de:bk095/InteraktiveMedien.git'
    }
    
    stage ('Build images') {
        sh 'docker-compose -f docker-compose.prod.yml build'
    }
    
    stage ('API tests') {
        sh 'docker-compose -f docker-compose.backend-testing-api.yml rm -fsv'
        sh 'docker-compose -f docker-compose.backend-testing-api.yml build'
        sh 'docker-compose -f docker-compose.backend-testing-api.yml up --abort-on-container-exit'
        sh 'docker-compose -f docker-compose.backend-testing-api.yml rm -fsv'
    }
        
    stage ('Store images') {
        sh 'docker-compose push'
    }
    
    stage ('Deploy images') {
        sh 'docker-compose stop'
        sh 'docker-compose rm -sf'
        sh 'docker-compose pull'
        sh 'docker-compose up -d'
    }
    
    stage ('Notification'){
        slackSend channel: '#jenkins', message: 'Backend and Web on master were released successfully'
    }
}
