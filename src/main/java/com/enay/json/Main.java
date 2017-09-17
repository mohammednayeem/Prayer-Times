package com.enay.json;

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

/**
 * Created by Mohammed on 8/2/2017.
 */
public class Main {
    private static String fajr, dhuhr, asr, maghrib, isha;
    private static int currentPrayer = 0;
    private static int timeTill = 0;
    private static final String[] PRAYERS = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
    private static ZoneId z = ZoneId.of("America/Montreal");
    private static LocalDate daylightdate;
    private static LocalDate today;

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

    private static OkHttpClient client = new OkHttpClient();

    private static String getJSON() throws IOException {
        Request request = new Request.Builder()
                .url("http://api.aladhan.com/timingsByCity?city=Nashua&country=USA&method=2")
                .build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private static void getPrayer() {
        currentPrayer = 0;
        String json;
        try {
            json = getJSON();
            JSONObject json1 = new JSONObject(json);
            JSONObject data = json1.getJSONObject("data");

            fajr = LocalTime.parse(data.getJSONObject("timings").getString("Fajr"), DateTimeFormatter.ofPattern("HH:mm")).toString();
            dhuhr = LocalTime.parse(data.getJSONObject("timings").getString("Dhuhr"), DateTimeFormatter.ofPattern("HH:mm")).toString();
            asr = LocalTime.parse(data.getJSONObject("timings").getString("Asr"), DateTimeFormatter.ofPattern("HH:mm")).toString();
            maghrib = LocalTime.parse(data.getJSONObject("timings").getString("Maghrib"), DateTimeFormatter.ofPattern("HH:mm")).toString();
            isha = LocalTime.parse(data.getJSONObject("timings").getString("Isha"), DateTimeFormatter.ofPattern("HH:mm")).toString();

            //For testing purposes
            //fajr = "";
            // dhuhr = "";
            // asr = "";
            //  maghrib = "16:56";
            // isha = "";

            getShortestTime();
            choosePrayer();
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Wasn't able to connect to API. Trying again......");
            getPrayer();
        }
    }

    private static int getDaylightSavingsTime(int num) {
        if (today.equals(daylightdate) && daylightdate.getMonthValue() == 11) {
            return num + 3600000;
        } else if (today.equals(daylightdate) && daylightdate.getMonthValue() == 3) {
            return num - 3600000;
        } else {
            return num;
        }
    }

    private static void recheckDaylightSavings() {
        ZoneRules rules = z.getRules();
        ZoneOffsetTransition nextTransition = rules.nextTransition(Instant.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/d/yyyy");
        daylightdate = LocalDate.parse(nextTransition.getInstant().atZone(z).format(DateTimeFormatter.ofPattern("M/d/y")), formatter);
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
                return "No more prayers for Today";
        }
    }

    private static void prayFajr() {
        currentPrayer = 0;
        int timeTillFajrh = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(fajr)).toHours());
        int timeTillFajrm = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(fajr)).toMinutes() - (timeTillFajrh * 60));
        int timeTillFajrms = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(fajr)).toMillis());
        today = LocalDate.now();
        if (today.equals(daylightdate.plusDays(1))) {
            recheckDaylightSavings();
        }
        timeTillFajrms = getDaylightSavingsTime(timeTillFajrms);
        if (timeTillFajrh == 0) {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + (timeTillFajrm + 1) + " minutes)");
        } else {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + timeTillFajrh + " hours and " + (timeTillFajrm + 1) + " minutes)");
        }
        try {
            Thread.sleep(timeTillFajrms);
            playAthan();
            Runtime.getRuntime().gc();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void prayDhuhr() {
        currentPrayer = 1;
        int timeTillDhuhrh = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(dhuhr)).toHours());
        int timeTillDhuhrm = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(dhuhr)).toMinutes() - (timeTillDhuhrh * 60));
        int timeTillDhuhrms = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(dhuhr)).toMillis());
        if (timeTillDhuhrh == 0) {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + (timeTillDhuhrm + 1) + " minutes)");
        } else {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + timeTillDhuhrh + " hours and " + (timeTillDhuhrm + 1) + " minutes)");
        }
        try {
            Thread.sleep(timeTillDhuhrms);
            playAthan();
            Runtime.getRuntime().gc();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void prayAsr() {
        currentPrayer = 2;
        int timeTillAsrh = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(asr)).toHours());
        int timeTillAsrm = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(asr)).toMinutes() - (timeTillAsrh * 60));
        int timeTillAsrms = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(asr)).toMillis());
        if (timeTillAsrh == 0) {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + (timeTillAsrm + 1) + " minutes)");
        } else {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + timeTillAsrh + " hours and " + (timeTillAsrm + 1) + " minutes)");
        }
        try {
            Thread.sleep(timeTillAsrms);
            playAthan();
            Runtime.getRuntime().gc();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void prayMaghrib() {
        currentPrayer = 3;
        int timeTillMaghribh = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(maghrib)).toHours());
        int timeTillMaghribm = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(maghrib)).toMinutes() - (timeTillMaghribh * 60));
        int timeTillMaghribms = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(maghrib)).toMillis());
        if (timeTillMaghribh == 0) {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + (timeTillMaghribm + 1) + " minutes)");
        } else {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + timeTillMaghribh + " hours and " + (timeTillMaghribm + 1) + " minutes)");
        }
        try {
            Thread.sleep(timeTillMaghribms);
            playAthan();
            Runtime.getRuntime().gc();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void prayIsha() {
        currentPrayer = 4;
        int timeTillIshah = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(isha)).toHours());
        int timeTillIsham = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(isha)).toMinutes() - (timeTillIshah * 60));
        int timeTillIshams = (int) Math.abs(Duration.between(LocalTime.now(), LocalTime.parse(isha)).toMillis());
        if (timeTillIshah == 0) {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + (timeTillIsham + 1) + " minutes)");
        } else {
            System.out.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "] " + PRAYERS[currentPrayer] + " is at " + toAmerican(currentPrayer) + " (" + timeTillIshah + " hours and " + (timeTillIsham + 1) + " minutes)");
        }
        try {
            Thread.sleep(timeTillIshams);
            playAthan();
            Runtime.getRuntime().gc();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/d/yyyy");
        daylightdate = LocalDate.parse(nextTransition.getInstant().atZone(z).format(DateTimeFormatter.ofPattern("M/d/y")), formatter);
        Bismillah();
        Runtime.getRuntime().gc();
        while (true) {
            getPrayer();
            System.out.println(LocalDate.now().format(DateTimeFormatter.ofPattern(("M/d/y"))));
        }


    }
}