package com.capel.aprendiz;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço em primeiro plano que toca, em sequência, os áudios gravados (voz neural)
 * de perguntas e respostas, com as pausas entre eles. Roda com a tela apagada.
 */
public class LeitorService extends Service {

    public interface Listener { void onLeitorEvent(String type, String id, String msg); }

    public static class Item {
        final String id; final String file; final int pauseMs;
        public Item(String id, String file, int pauseMs) { this.id = id; this.file = file; this.pauseMs = pauseMs; }
    }

    public static final String ACTION_PLAY = "com.capel.aprendiz.PLAY";
    public static final String ACTION_STOP = "com.capel.aprendiz.STOP";
    private static final String CHANNEL = "leitura";
    private static final int NOTIF_ID = 33;

    static volatile Listener listener;
    static volatile LeitorService instance;
    static List<Item> pending = new ArrayList<>();
    static float rate = 1f;

    private final Handler main = new Handler(Looper.getMainLooper());
    private List<Item> items = new ArrayList<>();
    private MediaPlayer player;
    private PowerManager.WakeLock wakeLock;
    private int session = 0;
    private int lastStartId = 0;

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
            halt();
            emit("end", null, "stopped");
            finish();
            return START_NOT_STICKY;
        }
        goForeground();
        acquireWakeLock();
        halt();
        synchronized (LeitorService.class) { items = new ArrayList<>(pending); }
        final int s = session;
        main.post(() -> playIndex(0, s));
        return START_NOT_STICKY;
    }

    private void playIndex(int i, int s) {
        if (s != session) return;
        if (i >= items.size()) {
            emit("end", null, "complete");
            finish();
            return;
        }
        final Item it = items.get(i);
        emit("start", it.id, null);
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            try (AssetFileDescriptor afd = getAssets().openFd("public/" + it.file)) {
                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            }
            mp.prepare();
            mp.setOnCompletionListener(p -> {
                p.release();
                if (player == p) player = null;
                if (s != session) return;
                emit("done", it.id, null);
                main.postDelayed(() -> playIndex(i + 1, s), Math.max(0, it.pauseMs));
            });
            player = mp;
            if (Math.abs(rate - 1f) > 0.01f) {
                mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(rate));
            }
            mp.start();
        } catch (Exception e) {
            emit("erro", it.id, "Não foi possível tocar " + it.file + ": " + e.getMessage());
            main.postDelayed(() -> playIndex(i + 1, s), 300);
        }
    }

    /** Interrompe o que estiver tocando ou agendado. */
    private void halt() {
        session++;
        main.removeCallbacksAndMessages(null);
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) { }
            player.release();
            player = null;
        }
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
        halt();
        finish();
    }

    @Override
    public void onDestroy() {
        instance = null;
        halt();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        super.onDestroy();
    }
}
