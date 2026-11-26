package com.capel.aprendiz;

import android.content.Intent;

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

@CapacitorPlugin(name = "Leitor")
public class LeitorPlugin extends Plugin implements LeitorService.Listener {

    @Override
    public void load() { LeitorService.listener = this; }

    @Override
    public void onLeitorEvent(String type, String id, String msg) {
        JSObject o = new JSObject();
        o.put("type", type);
        if (id != null) o.put("id", id);
        if (msg != null) o.put("msg", msg);
        notifyListeners("leitor", o);
    }

    /** items: [{id, file (caminho em www/), pauseMs}], rate: velocidade (1 = normal). */
    @PluginMethod
    public void play(PluginCall call) {
        JSArray arr = call.getArray("items");
        if (arr == null || arr.length() == 0) { call.reject("Fila vazia"); return; }
        List<LeitorService.Item> items = new ArrayList<>();
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                items.add(new LeitorService.Item(o.getString("id"), o.getString("file"), o.optInt("pauseMs", 0)));
            }
        } catch (Exception e) { call.reject("Fila inválida", e); return; }
        synchronized (LeitorService.class) { LeitorService.pending = items; }
        Double r = call.getDouble("rate", 1.0);
        LeitorService.rate = r == null ? 1f : r.floatValue();
        LeitorService.listener = this;
        ContextCompat.startForegroundService(getContext(),
                new Intent(getContext(), LeitorService.class).setAction(LeitorService.ACTION_PLAY));
        call.resolve();
    }

    @PluginMethod
    public void stop(PluginCall call) {
        LeitorService s = LeitorService.instance;
        if (s != null) s.stopNow();
        call.resolve();
    }

    @Override
    protected void handleOnDestroy() {
        if (LeitorService.listener == this) LeitorService.listener = null;
    }
}
