package org.kopuz.transfer_zone.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.kopuz.transfer_zone.entity.TeamList;
import org.kopuz.transfer_zone.service.TwitterApiService;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TransferBot {
    private final TelegramBot bot;
    private final TwitterApiService twitterApiService;
    private final Environment env;

    private final String botToken;
    private Map<Long, List<String>> storedUserTeams = new HashMap<>();

    public TransferBot(TwitterApiService twitterApiService, Environment env) {
        this.env = env;

        botToken = this.env.getProperty("telegram.bot.token");

        this.twitterApiService = twitterApiService;

        this.bot = new TelegramBot(botToken);

    }

    @PostConstruct
    public void init() {

        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                handleUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
            // Create Exception Handler
        }, e -> {
            if (e.response() != null) {
                // got bad response from telegram
                e.response().errorCode();
                e.response().description();
            } else {
                // probably network error
                e.printStackTrace();
            }
        });
    }

    @Async
    public void handleUpdate(Update update){
        Message message = update.message();
        if (message == null || message.text() == null) return;

        Chat chat = message.chat();
        Long chatId = chat.id();
        String text = message.text();
        String[] userWords = text.split("\\s+");
        List<String> userTeams = new ArrayList<>();

        if(text.startsWith("/setteam")){

            if(userWords.length <= 1){
                String warnMessage = "❗ Bir takım seçiminde bulunmadınız.\n" +
                        "❓ Takım seçiminde bulunmak adına ÖRNEK: '/setteam TakimAdi1 TakimAdi2'";

                bot.execute(new SendMessage(chatId, warnMessage);
            }

            for(int i=1; i < userWords.length; i++){

                userTeams.add(userWords[i]);
            }

            if(TeamList.teams.contains(userTeams)){

                String successMessage = "Haber almak istediğiniz takimlar sisteme eklenmiştir:\n"
                        + "\uD83D\uDEE1\uFE0F " + userTeams;

                try{

                    String outputMessage = twitterApiService.findTweetForFavTeams(userTeams);

                    bot.execute(new SendMessage(chatId, successMessage));
                    bot.execute(new SendMessage(chatId, outputMessage);


                    storedUserTeams.put(chatId, userTeams);

                }catch (Exception e){
                    e.printStackTrace();
                }


            }else {
                String errorMessage = "Specified team name is not found!";

                bot.execute(new SendMessage(chatId, errorMessage));
            }

        }
        else{
            String errorMessage = "Specified command is not found! If you want to set your team, use '/setteam'";

            bot.execute(new SendMessage(chatId, errorMessage));
        }

    }

    @Async
    @Scheduled(fixedRate = 1000000)
    public void sendTransferUpdates(){
        if(storedUserTeams.isEmpty()){
            return;
            // logger comes here
        }

        for(var userValues : storedUserTeams.entrySet()){
            Long chatId = userValues.getKey();
            List<String> userTeams = userValues.getValue();

            try{
                String outputMessage = twitterApiService.findTweetForFavTeams(userTeams);

                if (!outputMessage.isEmpty()) {

                    bot.execute(new SendMessage(chatId, outputMessage));

                } else {
                    // logger message comes here
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }



}
