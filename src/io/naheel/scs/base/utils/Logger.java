package io.naheel.scs.base.utils;

import java.util.ArrayList;
import java.util.List;

public class Logger {

    public interface Listener {
        void onLog(String log);
    }

    private List<String> logs = new ArrayList<>();
    private List<Listener> listeners = new ArrayList<>();

    public void log(String s) {
        logs.add(s);
        runListeners(s);
    }

    public void log(String s, Object... args) {
        log(String.format(s, args));
    }

    public void connect(Listener l) {
        listeners.add(l);
    }

    private void runListeners(String s) {
        for (Listener l : listeners)
            l.onLog(s);
    }

    public void clear() {
        logs.clear();
        runListeners(null);
    }

    public int size() {
        return logs.size();
    }

    public String get(int i) {
        return logs.get(i);
    }

    public void save(String path) {
        Utils.writeFile((path == null ? Utils.getDir() : path) + "/out_log.txt", toString() + "\n\n", false);
    }

    public void save() {
        save(null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : logs)
            sb.append(s).append('\n');
        return sb.toString();
    }

}