def call() {

    pipeline {
        agent any

        stages {

            stage('Load Configuration') {
                steps {
                    script {
                        def yamlText = libraryResource('kafka-config.yaml')
                        env.CONFIG = readYaml(text: yamlText)
                    }
                }
            }

            stage('Clone Repository') {
                steps {
                    git url: env.CONFIG.REPO_URL, branch: env.CONFIG.BRANCH
                }
            }

            stage('User Approval') {
                when {
                    expression { env.CONFIG.KEEP_APPROVAL_STAGE }
                }
                steps {
                    input message: "Approve Kafka deployment to ${env.CONFIG.ENVIRONMENT}?"
                }
            }

            stage('Kafka Ansible Execution') {
                steps {
                    sh """
                      ansible-playbook \
                      ${env.CONFIG.CODE_BASE_PATH}/kafka-playbook.yml \
                      -i ${env.CONFIG.CODE_BASE_PATH}/inventory
                    """
                }
            }

            stage('Notification') {
                steps {
                    slackSend(
                        channel: env.CONFIG.SLACK_CHANNEL_NAME,
                        message: env.CONFIG.ACTION_MESSAGE
                    )
                }
            }
        }
    }
}

