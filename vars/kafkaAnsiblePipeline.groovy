def call() {

    pipeline {
        agent any

        stages {

            stage('Load Configuration') {
                steps {
                    script {
                        def yamlText = libraryResource('kafka-config.yaml')
                        CONFIG = readYaml(text: yamlText)
                    }
                }
            }

            stage('Clone Repository') {
                steps {
                    git url: CONFIG.REPO_URL, branch: CONFIG.BRANCH
                }
            }

            stage('User Approval') {
                when {
                    expression { CONFIG.KEEP_APPROVAL_STAGE }
                }
                steps {
                    input message: "Approve Kafka deployment to ${CONFIG.ENVIRONMENT}?"
                }
            }

            stage('Kafka Ansible Execution') {
                steps {
                    sh """
                      ansible-playbook \
                      ${CONFIG.CODE_BASE_PATH}/kafka-playbook.yml \
                      -i ${CONFIG.CODE_BASE_PATH}/inventory
                    """
                }
            }

            stage('Notification') {
                steps {
                    slackSend(
                        channel: CONFIG.SLACK_CHANNEL_NAME,
                        message: CONFIG.ACTION_MESSAGE
                    )
                }
            }
        }
    }
}

