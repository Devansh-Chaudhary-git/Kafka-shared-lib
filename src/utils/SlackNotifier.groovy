package utils

class SlackNotifier implements Serializable {

    def send(String channel, String message) {
        slackSend(
            channel: channel,
            message: message
        )
    }
}

