
package org.example;

import org.apache.log4j.PropertyConfigurator;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Properties;

public class Main {

    public static final String BOT_USERNAME = System.getenv("BOT_USERNAME");
    public static final String BOT_TOKEN = System.getenv("BOT_TOKEN");

    public static void main(String[] args) {
        Properties log4jProp = new Properties();
        log4jProp.setProperty("log4j.rootLogger", "WARN");
        PropertyConfigurator.configure(log4jProp);
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new MainBot(BOT_TOKEN, BOT_USERNAME));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

