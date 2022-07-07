public interface TelegramBotUtils {

    String BOT_USERNAME = "t.me/car_fine_self_bot";
    String BOT_TOKEN = "5232601790:AAEP0WBhQzsYoKZjjbleIqOsZl7YTilE-OY";

    default boolean isStart(String text){
        return text.equals("/start");
    }

    default boolean isMenu(String text){
        return text.equals("Menu");
    }




}
