package ru.yooxa.bungee.auth;


public class Utils {
    public static String leftTime(long unixTime, boolean b) {
        long seconds;
        if (b) {
            seconds = unixTime - System.currentTimeMillis() / 1000L;
        } else {
            seconds = System.currentTimeMillis() / 1000L - unixTime;
        }

        long minutes = 0L;
        long hours = 0L;
        long days = 0L;

        if (seconds >= 60L) {
            long i = (int) (seconds / 60L);
            minutes = i;
            seconds -= i * 60L;
        }

        if (minutes >= 60L) {
            long i = (int) (minutes / 60L);
            hours = i;
            minutes -= i * 60L;
        }

        if (hours >= 24L) {
            long i = (int) (hours / 24L);
            days = i;
            hours -= i * 24L;
        }

        String s = "";


        if (days > 0L) {
            String msg = "дней";
            if (days >= 11L && days <= 14L) {
                int count = (int) (days % 10L);
                switch (count) {
                    case 1:
                        msg = "день";
                        break;
                    case 2:
                    case 3:
                    case 4:
                        msg = "дня";
                        break;
                }
            }
            s = s + days + " " + msg + " ";
        }

        if (hours > 0L) {
            String msg = "часов";
            if (hours >= 11L && hours <= 14L) {
                int count = (int) (hours % 10L);
                switch (count) {
                    case 1:
                        msg = "час";
                        break;
                    case 2:
                    case 3:
                    case 4:
                        msg = "часа";
                        break;
                }
            }
            s = s + hours + " " + msg + " ";
        }

        if (minutes > 0L) {
            String msg = "минут";
            if (minutes >= 11L && minutes <= 14L) {
                int count = (int) (minutes % 10L);
                switch (count) {
                    case 1:
                        msg = msg + "а";
                        break;
                    case 2:
                    case 3:
                    case 4:
                        msg = msg + "ы";
                        break;
                }
            }
            s = s + minutes + " " + msg + " ";
        }

        if (seconds > 0L) {
            String msg = "секунд";
            if (seconds >= 11L && seconds <= 14L) {
                int count = (int) (seconds % 10L);
                switch (count) {
                    case 1:
                        msg = msg + "а";
                        break;
                    case 2:
                    case 3:
                    case 4:
                        msg = msg + "ы";
                        break;
                }
            }
            s = s + seconds + " " + msg;
        }

        return s;
    }
}


