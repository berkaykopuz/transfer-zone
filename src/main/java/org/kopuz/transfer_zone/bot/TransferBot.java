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
import java.util.stream.Collectors;

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
        List<String> userTeams = new ArrayList<>();

        if(text.startsWith("/setteam")){

            String[] userWords = text.substring(9).split(", ");

            if(userWords.length < 1){
                String warnMessage = "❗ Bir takım seçiminde bulunmadınız.\n\n" +
                        "❓ Takım seçiminde bulunmak adına ÖRNEK: '/setteam TakimAdi1 TakimAdi2'";

                bot.execute(new SendMessage(chatId, warnMessage));

                return;
            }

            for(int i=0; i < userWords.length; i++){
                userTeams.add(userWords[i]);
            }

            if(TeamList.teams.containsAll(userTeams.stream().map(String::toLowerCase).collect(Collectors.toList()))){

                String successMessage = "Haber almak istediğiniz takimlar sisteme eklenmiştir:\n\n"
                        + "\uD83D\uDEE1\uFE0F " + userTeams;

                try{

                    String outputMessage = twitterApiService
                            .findTweetForFavTeams(userTeams.stream().map(String::toLowerCase).collect(Collectors.toList()));

                    bot.execute(new SendMessage(chatId, successMessage));
                    bot.execute(new SendMessage(chatId, outputMessage));

                    storedUserTeams.put(chatId, userTeams);

                }catch (Exception e){
                    e.printStackTrace();
                }


            }else {
                String errorMessage = "Belirtilen takım adı listede bulunamamıştır!";

                bot.execute(new SendMessage(chatId, errorMessage));
            }

        }
        else{
            String errorMessage = "Belirtilen komut bulunamadı! Eğer takımınızı kaydetmek isterseniz, " +
                    "'/setteam TakimAdi' komutunu kullanabilirsiniz.";

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
                String outputMessage = twitterApiService
                        .findTweetForFavTeams(userTeams.stream().map(String::toLowerCase).collect(Collectors.toList()));

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
