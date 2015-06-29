package com.ubercalendar.util;

import android.content.SharedPreferences;

import com.ubercalendar.model.User;

/**
 * Created by julian on 6/28/15.
 */
public enum StringValues {
    TOKEN("token"),
    TOKEN_TYPE("token_type");
    private final String key;
    private String value;
    private static SharedPreferences pref;
    StringValues(String key) {
        this.key = key;
    }
    public void set(String value) {
        this.value = value;
        pref.edit().putString(key, value).apply();
    }
    public void load() {
        value = pref.getString(key, "");
    }
    public String get() {
        return value;
    }

    public static void init(SharedPreferences pref) {
        StringValues.pref = pref;
        TOKEN.load();
        TOKEN_TYPE.load();
    }
    public static boolean updateToken(User user) {
        TOKEN.set(user.getAccessToken());
        TOKEN_TYPE.set(user.getTokenType());
        return true;
    }

}
