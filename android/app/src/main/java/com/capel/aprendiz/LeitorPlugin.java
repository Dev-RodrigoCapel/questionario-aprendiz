package com.capel.aprendiz;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import androidx.core.content.ContextCompat;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@CapacitorPlugin(name = "Leitor")
public class LeitorPlugin extends Plugin implements LeitorService.Listener {

    private TextToSpeech probe;
    private boolean probeReady = false;
    private final List<PluginCall> voiceCalls = new ArrayList<>();

    @Override
    public void load() {
        LeitorService.listener = this;
    }

    @Override
    public void onLeitorEvent(String type, String id, String msg) {
        JSObject o = new JSObject();
        o.put("type", type);
        if (id != null) o.put("id", id);
        if (msg != null) o.put("msg", msg);
        notifyListeners("leitor", o);
    }

    @PluginMethod
    public void play(PluginCall call) {
        JSArray arr = call.getArray("items");
        if (arr == null || arr.length() == 0) { call.reject("Fila vazia"); return; }
        List<LeitorService.Item> items = new ArrayList<>();
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                items.add(new LeitorService.Item(o.getString("id"), o.getString("text"), o.optInt("pauseMs", 0)));
            }
        } catch (Exception e) { call.reject("Fila inválida", e); return; }
        synchronized (LeitorService.class) { LeitorService.pending = items; }
        Double r = call.getDouble("rate", 1.0);
        LeitorService.rate = r == null ? 1f : r.floatValue();
        LeitorService.voiceName = call.getString("voice", null);
        LeitorService.listener = this;
        Intent it = new Intent(getContext(), LeitorService.class).setAction(LeitorService.ACTION_PLAY);
        ContextCompat.startForegroundService(getContext(), it);
        call.resolve();
    }

    @PluginMethod
    public void stop(PluginCall call) {
        LeitorService s = LeitorService.instance;
        if (s != null) s.stopNow();
        call.resolve();
    }

    @PluginMethod
    public void getVoices(PluginCall call) {
        if (probeReady) { resolveVoices(call); return; }
        voiceCalls.add(call);
        if (probe == null) {
            probe = new TextToSpeech(getContext().getApplicationContext(), status -> {
                probeReady = status == TextToSpeech.SUCCESS;
                for (PluginCall c : new ArrayList<>(voiceCalls)) resolveVoices(c);
                voiceCalls.clear();
            });
        }
    }

    private void resolveVoices(PluginCall call) {
        JSArray list = new JSArray();
        if (probeReady && probe.getVoices() != null) {
            for (Voice v : probe.getVoices()) {
                Locale l = v.getLocale();
                if (l == null || !"pt".equals(l.getLanguage())) continue;
                if (v.getFeatures() != null && v.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)) continue;
                JSObject o = new JSObject();
                o.put("name", v.getName());
                o.put("lang", l.toLanguageTag());
                o.put("network", v.isNetworkConnectionRequired());
                o.put("quality", v.getQuality());
                list.put(o);
            }
        }
        JSObject res = new JSObject();
        res.put("voices", list);
        res.put("ready", probeReady);
        call.resolve(res);
    }

    @Override
    protected void handleOnDestroy() {
        if (LeitorService.listener == this) LeitorService.listener = null;
        if (probe != null) { probe.shutdown(); probe = null; }
    }
}
