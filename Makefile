build:
	@docker build -t hub.playgroundvina.com/pg/$(NAME):$(ENV) -f docker/Dockerfile . --no-cache

# Telegram Notify
notify_start:
	@curl -X POST -H 'Content-Type: application/json' -d '{"chat_id": "$(CHAT_ID)", "text": "$(JOB_NAME): #$(BUILD_NUMBER)\n=====\nStarted!", "disable_notification": false}' "https://api.telegram.org/bot$(TOKEN)/sendMessage"

notify_success:
	@curl -X POST -H 'Content-Type: application/json' -d '{"chat_id": "$(CHAT_ID)", "text": "$(JOB_NAME): #$(BUILD_NUMBER)\n<a href=\"'"${APK_LINK}"'\">apk</a>\n<a href=\"'"${AAB_LINK}"'\">aab</a>\n=====\n✅ Deploy succeeded!", "disable_notification": false, "parse_mode": "HTML"}' "https://api.telegram.org/bot$(TOKEN)/sendMessage"

notify_failure:
	@curl -X POST -H 'Content-Type: application/json' -d '{"chat_id": "$(CHAT_ID)", "text": "$(JOB_NAME): #$(BUILD_NUMBER)\n=====\n❌ Deploy failure!", "disable_notification": false}' "https://api.telegram.org/bot$(TOKEN)/sendMessage"

notify_aborted:
	@curl -X POST -H 'Content-Type: application/json' -d '{"chat_id": "$(CHAT_ID)", "text": "$(JOB_NAME): #$(BUILD_NUMBER)\n=====\n❌ Deploy aborted!", "disable_notification": false}' "https://api.telegram.org/bot$(TOKEN)/sendMessage"

