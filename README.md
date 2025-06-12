# ⚽ Transfer Zone

This bot follows popular football related reporters on X platform and submits their transfer rumor entries to the user as a notification on Telegram.

## 📋 Properties

- 🔍 Scans specified team related tweets from reporters per 15 minutes
- 💾 Multiple teams can be selected to follow news
- 🚫 Selected or all teams can be removed from tracking list
- 📱 Live notifications from Telegram

## 🛠 System Requirements

### Software Requirements

- Java (17 or a higher version)
- Spring Boot
- Telegram Bot Token
- Twitter Developer App Token

You need one of the following options for bot to work continuously:

1. **VPS (Virtual Private Server) - Recommended 🌟**

  - 24/7 uninterrupted work
  - low cost 

2. **Personal Computer**

   - It must remain open 24/7
   - Internet connection should be continuous
   - The computer should be prevented from switching to sleep mode

## 🛠️ Instructions

### Basically Usage of The Application
If you wish to use my twitter rights of limit I welcome it :) . Then you can basically join to the [link](https://t.me/transferZoneBot) and enjoy it. This might be a bit interrupted experience though.
On the other hand; if you are intending to use your own account, follow next steps.

### 1. Creation of Telegram Bot

Start a conversation with [@botfather](https://t.me/botfather) in Telegram 1.
2. Send `/Newbot` command
3. Set a name for the bot
4. Set a user name for bot
5. Botfather will give you a ** API token **, this is your Telegram Token that is mentioned above.

### 2. Telegram Kanal ID'si Alma

Open your browser and go to: https://developer.twitter.com

1. Sign in with your existing X (Twitter) account or create a new one.
2. Once signed in, go to the Developer Portal (top menu) or navigate to Projects & Apps > Overview.
3. Click on "Add App" or "Create App" under your project.
4. Enter a name for your app (e.g., TransferZoneBot) and proceed.
5. Choose your app environment and permissions (Read should be fine).
6. After creation, go to the "Keys and Tokens" tab of your app.
7. Store and secure the Client ID and Client Secret displayed under the OAuth 2.0 section.
8. These will be used for main program.

### 3. Instruction of Project 

1. Download or clone the project:

```bash
git clone https://github.com/berkaykopuz/transfer-zone
cd transfer-zone
```

2. Edit application.properties file just as below:

```application.properties
spring.application.name=transfer-zone
telegram.bot.token=YOUR TELEGRAM TOKEN
twitter.client.id=YOUR CLIENT ID
twitter.client.secret=YOUR CLIENT SECRET
twitter.oauth.callback.url=http://localhost:8080/auth/callback
twitter.oauth.scopes=offline.access tweet.read users.read
```

3. Run the program and you will see a link to authorize your developer account in java application console. 

4. Click on that link, then allow your application to pass.
   
5. You will see a '&code=....' section in your url after be redirected to another page. Copy the section '...' then paste it onto your application console.
   
6. If you are seeing your access and refresh token, you are ready.

7. Open your telegram bot and follow the command instructions. Enjoy it.

## 📱 An Example of Notification

![image](https://github.com/user-attachments/assets/5f7ae6d6-50ea-43d0-9855-e0c7bae37cbb)

## 🚨 Error Declaration

If you have found an error or if you have a suggestion, please mention through GitHub [Open issue](https://github.com/berkaykopuz/transfer-zone/issues).

## 📄 License

This project is licensed under MIT license. For more information, see the [LICENSE](LICENSE) file.
