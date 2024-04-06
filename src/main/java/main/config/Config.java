package main.config;

public class Config {

    private static final String DEV_BOT_TOKEN = System.getenv("DEV_TOKEN");
    private static final String PRODUCTION_BOT_TOKEN = System.getenv("TOKEN");
    private static final String TOKEN = PRODUCTION_BOT_TOKEN;
    private static final String TOP_GG_API_TOKEN = System.getenv("TOP_GG_API_TOKEN");
    private static final String BOT_ID = "808277484524011531"; //megoDev: 780145910764142613 //giveaway: 808277484524011531

    private static final String DATABASE_URL_DEV = System.getenv("DATABASE_URL_DEV");
    private static final String DATABASE_URL = System.getenv("DATABASE_URL");

    private static final String DATABASE_USER_DEV = System.getenv("DATABASE_USER_DEV");
    private static final String DATABASE_USER = System.getenv("DATABASE_USER");

    private static final String DATABASE_PASS = System.getenv("DATABASE_PASS");

    private static final String BOTICORD = System.getenv("BOTICORD");

    private static volatile boolean IS_DEV = true;

    static {
        if (TOKEN.equals(PRODUCTION_BOT_TOKEN)) {
            IS_DEV = false;
        }
    }

    public static String getDatabaseUrl() {
        if (IS_DEV) return DATABASE_URL_DEV;
        else return DATABASE_URL;
    }

    public static String getDatabaseUser() {
        if (IS_DEV) return DATABASE_USER_DEV;
        else return DATABASE_USER;
    }

    public static String getDatabasePass() {
        return DATABASE_PASS;
    }

    public static String getTOKEN() {
        return TOKEN;
    }

    public static String getTopGgApiToken() {
        return TOP_GG_API_TOKEN;
    }

    public static String getBotId() {
        return BOT_ID;
    }

    public static boolean isIsDev() {
        return IS_DEV;
    }

    public static String getBoticord() {
        return BOTICORD;
    }
}