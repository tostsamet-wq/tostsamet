package com.ScarletWork.CounterFlame.menu;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** In-game menu UI. Toggle values are local UI state until game-side hooks are added. */
public final class MenuOverlay {
    private static final int BG = Color.rgb(22, 23, 27);
    private static final int PANEL = Color.rgb(31, 33, 39);
    private static final int TEXT = Color.rgb(244, 245, 247);
    private static final int MUTED = Color.rgb(163, 166, 174);
    private static final int ACCENT = Color.rgb(224, 68, 68);
    private static final String PREFS = "cf_menu_settings";

    private MenuOverlay() {}

    /** Call once from UnityPlayerActivity.onCreate(), after super.onCreate(...). */
    public static void attach(Activity activity, String rawConfigUrl) {
        activity.runOnUiThread(() -> new MenuOverlay().install(activity, rawConfigUrl));
    }

    private void install(Activity activity, String configUrl) {
        View content = activity.findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup)) return;
        ViewGroup host = (ViewGroup) content;
        if (host.findViewWithTag("cf_menu_overlay") != null) return;

        FrameLayout layer = new FrameLayout(activity);
        layer.setTag("cf_menu_overlay");
        layer.setClipChildren(false);
        layer.setClipToPadding(false);

        Button launcher = new Button(activity);
        launcher.setText("CF");
        launcher.setTextColor(Color.WHITE);
        launcher.setTextSize(14);
        launcher.setAllCaps(false);
        launcher.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        launcher.setBackground(rounded(activity, ACCENT, 24));
        FrameLayout.LayoutParams launcherLp = new FrameLayout.LayoutParams(dp(activity, 54), dp(activity, 54), Gravity.LEFT | Gravity.CENTER_VERTICAL);
        launcherLp.leftMargin = dp(activity, 12);
        layer.addView(launcher, launcherLp);

        LinearLayout panel = new LinearLayout(activity);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(activity, 14), dp(activity, 12), dp(activity, 14), dp(activity, 10));
        panel.setBackground(rounded(activity, BG, 14));
        panel.setVisibility(View.GONE);
        panel.setElevation(dp(activity, 12));
        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(dp(activity, 300), dp(activity, 430), Gravity.CENTER);
        layer.addView(panel, panelLp);

        TextView header = new TextView(activity);
        header.setText("COUNTER FLAME  ·  MENU");
        header.setTextColor(TEXT);
        header.setTextSize(16);
        header.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        header.setPadding(0, 0, 0, dp(activity, 2));
        panel.addView(header);

        TextView hint = new TextView(activity);
        hint.setText("Ayarlar cihazda saklanır");
        hint.setTextColor(MUTED);
        hint.setTextSize(11);
        hint.setPadding(0, 0, 0, dp(activity, 8));
        panel.addView(hint);

        ScrollView scroll = new ScrollView(activity);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        panel.addView(scroll, scrollLp);
        LinearLayout rows = new LinearLayout(activity);
        rows.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(rows);
        addDefaultOptions(activity, rows);

        Button close = new Button(activity);
        close.setText("Kapat");
        close.setTextColor(TEXT);
        close.setAllCaps(false);
        close.setBackground(rounded(activity, PANEL, 10));
        LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 42));
        closeLp.topMargin = dp(activity, 8);
        panel.addView(close, closeLp);

        launcher.setOnClickListener(v -> panel.setVisibility(panel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        close.setOnClickListener(v -> panel.setVisibility(View.GONE));
        host.addView(layer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (configUrl != null && configUrl.startsWith("https://raw.githubusercontent.com/")) {
            loadRemoteConfig(activity, rows, header, configUrl);
        }
    }

    private void addDefaultOptions(Activity activity, LinearLayout rows) {
        addSection(activity, rows, "COMBAT");
        addToggle(activity, rows, "Aimbot", "aimbot", false);
        addToggle(activity, rows, "Aim lock", "aim_lock", false);
        addToggle(activity, rows, "Magic bullet", "magic_bullet", false);
        addToggle(activity, rows, "Auto fire", "auto_fire", false);
        addToggle(activity, rows, "Aim circle", "aim_circle", false);
        addSection(activity, rows, "VISUALS");
        addToggle(activity, rows, "ESP", "esp", false);
        addToggle(activity, rows, "Streamer mode", "streamer_mode", false);
        addSection(activity, rows, "AIM POINT");
        RadioGroup group = new RadioGroup(activity);
        group.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton head = new RadioButton(activity);
        head.setId(View.generateViewId());
        head.setText("Head"); head.setTextColor(TEXT); head.setButtonTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        RadioButton body = new RadioButton(activity);
        body.setId(View.generateViewId());
        body.setText("Body"); body.setTextColor(TEXT); body.setButtonTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        group.addView(head); group.addView(body);
        group.check(head.getId());
        rows.addView(group);
    }

    private void addSection(Activity a, LinearLayout rows, String title) {
        TextView label = new TextView(a);
        label.setText(title);
        label.setTextColor(ACCENT);
        label.setTextSize(11);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        label.setPadding(0, dp(a, 12), 0, dp(a, 3));
        rows.addView(label);
    }

    private void addToggle(Activity a, LinearLayout rows, String title, String key, boolean initial) {
        android.widget.Switch toggle = new android.widget.Switch(a);
        toggle.setText(title);
        toggle.setTextColor(TEXT);
        toggle.setTextSize(14);
        toggle.setPadding(0, dp(a, 2), 0, dp(a, 2));
        toggle.setButtonTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        toggle.setChecked(a.getSharedPreferences(PREFS, 0).getBoolean(key, initial));
        toggle.setOnCheckedChangeListener((button, checked) ->
                a.getSharedPreferences(PREFS, 0).edit().putBoolean(key, checked).apply());
        rows.addView(toggle, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(a, 42)));
    }

    private void loadRemoteConfig(Activity activity, LinearLayout rows, TextView header, String configUrl) {
        Thread worker = new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(configUrl).openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() != 200) return;
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) json.append(line);
                reader.close();
                JSONObject root = new JSONObject(json.toString());
                String title = root.optString("title", "Counter Flame");
                activity.runOnUiThread(() -> {
                    header.setText((title + "  ·  MENU").toUpperCase());
                    renderConfig(activity, rows, root.optJSONArray("sections"));
                    Toast.makeText(activity, "Uzak menü ayarları yüklendi", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception ignored) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Uzak ayarlar yüklenemedi", Toast.LENGTH_SHORT).show());
            } finally {
                if (connection != null) connection.disconnect();
            }
        }, "cf-menu-config");
        worker.setDaemon(true);
        worker.start();
    }

    private void renderConfig(Activity activity, LinearLayout rows, JSONArray sections) {
        if (sections == null) return;
        rows.removeAllViews();
        Map<String, RadioGroup> choiceGroups = new HashMap<>();
        for (int i = 0; i < sections.length(); i++) {
            JSONObject section = sections.optJSONObject(i);
            if (section == null) continue;
            addSection(activity, rows, section.optString("title", "OPTIONS").toUpperCase());
            JSONArray items = section.optJSONArray("items");
            if (items == null) continue;
            for (int j = 0; j < items.length(); j++) {
                JSONObject item = items.optJSONObject(j);
                if (item == null) continue;
                String id = item.optString("id", "option_" + i + "_" + j);
                String label = item.optString("label", id);
                boolean initial = item.optBoolean("default", false);
                if ("choice".equals(item.optString("type"))) {
                    addChoice(activity, rows, choiceGroups, item, id, label, initial);
                } else {
                    addToggle(activity, rows, label, id, initial);
                }
            }
        }
    }

    private void addChoice(Activity activity, LinearLayout rows, Map<String, RadioGroup> groups,
                           JSONObject item, String id, String label, boolean initial) {
        String key = item.optString("group", "choices");
        RadioGroup group = groups.get(key);
        if (group == null) {
            group = new RadioGroup(activity);
            group.setOrientation(RadioGroup.HORIZONTAL);
            groups.put(key, group);
            rows.addView(group);
        }
        RadioButton choice = new RadioButton(activity);
        choice.setId(View.generateViewId());
        choice.setText(label);
        choice.setTextColor(TEXT);
        choice.setButtonTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        group.addView(choice);
        String saved = activity.getSharedPreferences(PREFS, 0).getString("choice_" + key, "");
        if (id.equals(saved) || (saved.isEmpty() && initial)) group.check(choice.getId());
        choice.setOnClickListener(v -> activity.getSharedPreferences(PREFS, 0).edit()
                .putString("choice_" + key, id).apply());
    }

    private static GradientDrawable rounded(Activity activity, int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(activity, radiusDp));
        return d;
    }

    private static int dp(Activity a, int value) {
        return (int) (value * a.getResources().getDisplayMetrics().density + 0.5f);
    }
}
