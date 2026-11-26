package com.capel.aprendiz;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;

import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Serviço em primeiro plano que lê a fila de perguntas/respostas com o TTS do Android.
 * A fila inteira (falas + pausas) é entregue ao motor de voz de uma vez, então a leitura
 * continua com a tela apagada, sem depender do JavaScript da WebView.
 */
public class LeitorService extends Service implements TextToSpeech.OnInitListener {

    public interface Listener { void onLeitorEvent(String type, String id, String msg); }

    public static class Item {
        final String id; final String text; final int pauseMs;
        public Item(String id, String text, int pauseMs) { this.id = id; this.text = text; this.pauseMs = pauseMs; }
    }

    public static final String ACTION_PLAY = "com.capel.aprendiz.PLAY";
    public static final String ACTION_STOP = "com.capel.aprendiz.STOP";
    private static final String CHANNEL = "leitura";
    private static final int NOTIF_ID = 33;

    static volatile Listener listener;
    static volatile LeitorService instance;
    static List<Item> pending = new ArrayList<>();
    static float rate = 1f;
    static String voiceName = null;

    private TextToSpeech tts;
    private boolean ready = false;
    private PowerManager.WakeLock wakeLock;
    private volatile String lastId = null;
    private int lastStartId = 0;
    private final Handler main = new Handler(Looper.getMainLooper());

    static void emit(String type, String id, String msg) {
        Listener l = listener;
        if (l != null) l.onLeitorEvent(type, id, msg);
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public void onCreate() { super.onCreate(); instance = this; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lastStartId = startId;
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_STOP.equals(action)) {
            if (tts != null) tts.stop();
            emit("end", null, "stopped");
            finish();
            return START_NOT_STICKY;
        }
        goForeground();
        acquireWakeLock();
        if (tts == null) {
            tts = new TextToSpeech(getApplicationContext(), this);
        } else if (ready) {
            speakAll();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            emit("erro", null, "Não foi possível iniciar o mecanismo de voz do Android.");
            finish();
            return;
        }
        ready = true;
        tts.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build());
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) { if (!id.contains("#")) emit("start", id, null); }
            @Override public void onDone(String id) { done(id); }
            @Override public void onError(String id) { done(id); }
            @Override public void onStop(String id, boolean interrupted) { }
        });
        speakAll();
    }

    private void done(String id) {
        if (!id.contains("#")) emit("done", id, null);
        if (id.equals(lastId)) {
            emit("end", null, "complete");
            main.post(this::finish);
        }
    }

    private void speakAll() {
        List<Item> items;
        synchronized (LeitorService.class) { items = new ArrayList<>(pending); }
        tts.stop();
        if (items.isEmpty()) { finish(); return; }
        tts.setSpeechRate(rate);
        boolean voiceSet = false;
        if (voiceName != null && tts.getVoices() != null) {
            for (Voice v : tts.getVoices()) {
                if (v.getName().equals(voiceName)) { voiceSet = tts.setVoice(v) == TextToSpeech.SUCCESS; break; }
            }
        }
        if (!voiceSet) {
            int r = tts.setLanguage(new Locale("pt", "BR"));
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                emit("erro", null, "A voz em português não está instalada. Abra Configurações > Saída de conversão de texto em voz e instale Português (Brasil).");
            }
        }
        String last = null;
        for (Item it : items) {
            tts.speak(it.text, TextToSpeech.QUEUE_ADD, null, it.id);
            last = it.id;
            if (it.pauseMs > 0) {
                String pid = it.id + "#p";
                tts.playSilentUtterance(it.pauseMs, TextToSpeech.QUEUE_ADD, pid);
                last = pid;
            }
        }
        lastId = last;
    }

    private void goForeground() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26 && nm.getNotificationChannel(CHANNEL) == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Leitura em voz alta", NotificationManager.IMPORTANCE_LOW);
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
        int pf = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), pf);
        PendingIntent stop = PendingIntent.getService(this, 1,
                new Intent(this, LeitorService.class).setAction(ACTION_STOP), pf);
        Notification n = new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .setContentTitle("Questionário do Aprendiz")
                .setContentText("Lendo perguntas e respostas")
                .setContentIntent(open)
                .addAction(android.R.drawable.ic_media_pause, "Parar", stop)
                .setOngoing(true)
                .setSilent(true)
                .build();
        int type = Build.VERSION.SDK_INT >= 29 ? ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK : 0;
        ServiceCompat.startForeground(this, NOTIF_ID, n, type);
    }

    private void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "aprendiz:leitura");
            wakeLock.setReferenceCounted(false);
        }
        wakeLock.acquire(3 * 60 * 60 * 1000L);
    }

    void finish() {
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
        stopSelf(lastStartId);
    }

    void stopNow() {
        lastId = null;
        if (tts != null) tts.stop();
        finish();
    }

    @Override
    public void onDestroy() {
        instance = null;
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
        super.onDestroy();
    }
}
