package com.enay.json;

import com.twilio.Twilio;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.util.Properties;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * Created by Mohammed on 8/2/2017.
 */
public class Main {
    private static final String[] PRAYERS = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
    private static String fajr, dhuhr, asr, maghrib, isha;
    private static int currentPrayer = 0;
    private static int timeTill = 0;
    private static String todayDateIslamic, fullTodayDateIslamic;
    private static ZoneId z = ZoneId.of("America/Montreal");
    private static LocalDate daylightdate;
    private static LocalDate prevdaylightdate;
    private static LocalDate today;
    private static OkHttpClient client = new OkHttpClient();
    public static final String ACCOUNT_SID = "SID";
    public static final String AUTH_TOKEN = "TOKEN";

    private static void Bismillah() throws Exception {
        try {
            File soundFile = new File(System.getProperty("user.home") + "/Desktop/Athan/Sounds/Bis.wav");
            AudioInputStream sound = AudioSystem.getAudioInputStream(soundFile);

            DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(sound);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    event.getLine().close();
                }
            });

            clip.start();
            Thread.sleep(clip.getMicrosecondLength() / 1000);
            if (clip.isOpen()) {
                clip.close();
                sound.close();
            }


        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e1) {
            e1.printStackTrace();
        }
    }

    private static void playAthan() throws Exception {
        if (currentPrayer == 0) {
            try {
                File soundFile = new File(System.getProperty("user.home") + "/Desktop/Athan/Sounds/FajrAthan.wav");
                AudioInputStream sound = AudioSystem.getAudioInputStream(soundFile);

                DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.open(sound);
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        event.getLine().close();
                    }
                });

                clip.start();
                Thread.sleep(clip.getMicrosecondLength() / 1000);
                if (clip.isOpen()) {
                    clip.close();
                    sound.close();
                }

            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e1) {
                e1.printStackTrace();
            }
        } else {
            try {
                File soundFile = new File(System.getProperty("user.home") + "/Desktop/Athan/Sounds/Athan.wav");
                AudioInputStream sound = AudioSystem.getAudioInputStream(soundFile);

                DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.open(sound);
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        event.getLine().close();
                    }
                });

                clip.start();
                Thread.sleep(clip.getMicrosecondLength() / 1000);
                if (clip.isOpen()) {
                    clip.close();
                    sound.close();
                }

            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static String getJSON(String website) throws IOException {
        Request request = new Request.Builder()
                .url(website)
                .build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private static void getPrayer(String web) {
        currentPrayer = 0;
        String json;
        try {
            json = getJSON(web);
            JSONObject json1 = new JSONObject(json);
            JSONObject data = json1.getJSONObject("data");

            fajr = LocalTime.parse(data.getJSONObject("timings").getString("Fajr"), DateTimeFormatter.ofPattern("HH:mm")).toString();
            dhuhr = LocalTime.parse(data.getJSONObject("timings").getString("Dhuhr"), DateTimeFormatter.ofPattern("HH:mm")).toString();
            asr = LocalTime.parse(data.getJSONObject("timings").getString("Asr"), DateTimeFormatter.ofPattern("HH:mm")).toString();
            maghrib = LocalTime.parse(data.getJSONObject("timings").getString("Maghrib"), DateTimeFormatter.ofPattern("HH:mm")).toString();
            isha = LocalTime.parse(data.getJSONObject("timings").getString("Isha"), DateTimeFormatter.ofPattern("HH:mm")).toString();
            todayDateIslamic = data.getJSONObject("timings").getString("TodayDate").toString();
            fullTodayDateIslamic = data.getJSONObject("timings").getString("FullTodayDate").toString();

            if(Integer.parseInt(todayDateIslamic) == 12) {
                sendText();
            }
            System.out.println(fullTodayDateIslamic);
            getShortestTime();
            choosePrayer();
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Can't connect, trying alternative API.......");
            getPrayer("http://api.aladhan.com/timingsByCity?city=Nashua&country=USA&method=2");
        }
    }
    private static void sendText() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message messageMe = Message.creator(new PhoneNumber("+NUMBER"),
                new PhoneNumber("+FROM"),
                "Today is " + fullTodayDateIslamic + ". Check when to fast.").create();
        Message message2 = Message.creator(new PhoneNumber("+NUMBER"),
                new PhoneNumber("+FROM"),
                "Today is " + fullTodayDateIslamic + ". Check when to fast.").create();
    }

    private static int getDaylightSavingsTime(int num) {
        if (today.equals(daylightdate) && daylightdate.getMonthValue() == 11 && !(prevdaylightdate.equals(today))) {
            return num + 3600000;
        } else if (today.equals(daylightdate) && daylightdate.getMonthValue() == 3 && !(prevdaylightdate.equals(today))) {
            return num - 3600000;
        } else {
            return num;
        }
    }

    private static void recheckDaylightSavings() {
        ZoneRules rules = z.getRules();
        ZoneOffsetTransition nextTransition = rules.nextTransition(Instant.now());
        ZoneOffsetTransition prevTransition = rules.previousTransition(Instant.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/d/yyyy");
        daylightdate = LocalDate.parse(nextTransition.getInstant().atZone(z).format(DateTimeFormatter.ofPattern("M/d/y")), formatter);
        prevdaylightdate = LocalDate.parse(prevTransition.getInstant().atZone(z).format(DateTimeFormatter.ofPattern("M/d/y")), formatter);
    }

    private static void getShortestTime() {
        int tdhuhr = (int) Duration.between(LocalTime.now(), LocalTime.parse(dhuhr)).toMinutes();
        int tasr = (int) Duration.between(LocalTime.now(), LocalTime.parse(asr)).toMinutes();
        int tmaghrib = (int) Duration.between(LocalTime.now(), LocalTime.parse(maghrib)).toMinutes();
        int tisha = (int) Duration.between(LocalTime.now(), LocalTime.parse(isha)).toMinutes();
        if ((tdhuhr < 0) && (tasr < 0) && (tmaghrib < 0) && (tisha < 0)) {
            currentPrayer = 5;
            return;
        }
        long elapsedMinutes = Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(fajr)).toMinutes());
        if ((elapsedMinutes >= Duration.between(LocalTime.now(), LocalTime.parse(dhuhr)).toMinutes()) && Duration.between(LocalTime.now(), LocalTime.parse(dhuhr)).toMinutes() > 0) {
            elapsedMinutes = Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(dhuhr)).toMinutes());
            currentPrayer = 1;
        }
        if ((elapsedMinutes >= Duration.between(LocalTime.now(), LocalTime.parse(asr)).toMinutes()) && Duration.between(LocalTime.now(), LocalTime.parse(asr)).toMinutes() > 0) {
            elapsedMinutes = Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(asr)).toMinutes());
            currentPrayer = 2;
        }
        if ((elapsedMinutes >= Duration.between(LocalTime.now(), LocalTime.parse(maghrib)).toMinutes()) && Duration.between(LocalTime.now(), LocalTime.parse(maghrib)).toMinutes() > 0) {
            elapsedMinutes = Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(maghrib)).toMinutes());
            currentPrayer = 3;
        }
        if ((elapsedMinutes >= Duration.between(LocalTime.now(), LocalTime.parse(isha)).toMinutes()) && Duration.between(LocalTime.now(), LocalTime.parse(isha)).toMinutes() > 0) {
            elapsedMinutes = Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(isha)).toMinutes());
            currentPrayer = 4;
        }

        timeTill = (int) elapsedMinutes;
    }

    private static String toAmerican(int prayer) {
        switch (prayer) {
            case 0:
                return LocalTime.parse(fajr, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("hh:mm a"));
            case 1:
                return LocalTime.parse(dhuhr, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("hh:mm a"));
            case 2:
                return LocalTime.parse(asr, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("hh:mm a"));
            case 3:
                return LocalTime.parse(maghrib, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("hh:mm a"));
            case 4:
                return LocalTime.parse(isha, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("hh:mm a"));
            default:
                return "Unknown Prayer";
        }
    }

    private static void printPrayer() {
        int timeTillh = -1;
        int timeTillm = -1;
        int timeTillms = -1;
        switch (currentPrayer) {
            case 0: //Fajr
                timeTillh = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(fajr)).toHours());
                timeTillm = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(fajr)).toMinutes() - (timeTillh * 60));
                timeTillms = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(fajr)).toMillis());
                today = LocalDate.now();
                if (today.equals(daylightdate.plusDays(1))) {
                    recheckDaylightSavings();
                }
                timeTillms = getDaylightSavingsTime(timeTillms);
                break;
            case 1: //Dhuhr
                timeTillh = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(dhuhr)).toHours());
                timeTillm = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(dhuhr)).toMinutes() - (timeTillh * 60));
                timeTillms = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(dhuhr)).toMillis());
                break;
            case 2: //Asr
                timeTillh = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(asr)).toHours());
                timeTillm = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(asr)).toMinutes() - (timeTillh * 60));
                timeTillms = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(asr)).toMillis());
                break;
            case 3: //Maghrib
                timeTillh = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(maghrib)).toHours());
                timeTillm = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(maghrib)).toMinutes() - (timeTillh * 60));
                timeTillms = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(maghrib)).toMillis());
                break;
            case 4: //Isha
                timeTillh = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(isha)).toHours());
                timeTillm = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(isha)).toMinutes() - (timeTillh * 60));
                timeTillms = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(isha)).toMillis());
                break;

        }
        if (timeTillh == 0) {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + (timeTillm + 1) + " minutes)");
        } else {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + timeTillh + " hours and " + (timeTillm + 1) + " minutes)");
        }
        try {
            Thread.sleep(timeTillms);
            playAthan();
            Runtime.getRuntime().gc();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void prayFajr() {
        currentPrayer = 0;
        printPrayer();
    }

    private static void prayDhuhr() {
        currentPrayer = 1;
        printPrayer();
    }

    private static void prayAsr() {
        currentPrayer = 2;
        printPrayer();
    }

    private static void prayMaghrib() {
        currentPrayer = 3;
        printPrayer();
    }

    private static void prayIsha() {
        currentPrayer = 4;
        printPrayer();
    }

    private static void noPrayer() {
        currentPrayer = 5;
        ZonedDateTime now = ZonedDateTime.now(z);
        LocalDate tomorrow = now.toLocalDate().plusDays(1);
        ZonedDateTime tomorrowStart = tomorrow.atStartOfDay(z);
        int timeTillMidnight = (int) Duration.between(now, tomorrowStart).toMillis();
        try {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] No more prayers tonight. Check back tomorrow");
            if ((int) Duration.between(now, tomorrowStart).toHours() == 0) {
                System.out.println(((int) Duration.between(now, tomorrowStart).toMinutes() - (((int) Duration.between(now, tomorrowStart).toHours()) * 60) + 1) + " minutes till midnight.\n\n");
            } else {
                System.out.println((int) Duration.between(now, tomorrowStart).toHours() + " hours and " + ((int) Duration.between(now, tomorrowStart).toMinutes() - (((int) Duration.between(now, tomorrowStart).toHours()) * 60) + 1) + " minutes till midnight.\n\n");
            }
            Thread.sleep(timeTillMidnight);
            Runtime.getRuntime().gc();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    private static void choosePrayer() {
        switch (currentPrayer) {
            case 0:
                prayFajr();
            case 1:
                prayDhuhr();
            case 2:
                prayAsr();
            case 3:
                prayMaghrib();
            case 4:
                prayIsha();
            case 5:
                noPrayer();
                break;
            default:
                System.out.println("Something broke");
                break;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Began on: " + LocalDate.now().format(DateTimeFormatter.ofPattern(("M/d/y"))) + " at " + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        ZoneRules rules = z.getRules();
        ZoneOffsetTransition nextTransition = rules.nextTransition(Instant.now());
        ZoneOffsetTransition prevTransition = rules.previousTransition(Instant.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/d/yyyy");
        daylightdate = LocalDate.parse(nextTransition.getInstant().atZone(z).format(DateTimeFormatter.ofPattern("MM/d/y")), formatter);
        prevdaylightdate = LocalDate.parse(prevTransition.getInstant().atZone(z).format(DateTimeFormatter.ofPattern("MM/d/y")), formatter);
        Bismillah();
        Runtime.getRuntime().gc();
        while (true) {
            getPrayer("http://isgnnh.org/islamicfinder/get.php");
            System.out.println(LocalDate.now().format(DateTimeFormatter.ofPattern(("M/d/y"))));
        }
    }
}