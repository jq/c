package com.ubercalendar.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 3/23/15.
 */
public class Util {
    public static boolean isValidEmailAddress(String email) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b");
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
    public static List<String> userEmailAccounts(Context context) {
        AccountManager accountManager = AccountManager.get(context);

        Account[] accounts = accountManager.getAccountsByType("com.google");
        List<String> emailAddresses = new ArrayList<String>(accounts.length);
        for (Account account : accounts) {
            emailAddresses.add(account.name);
        }
        return emailAddresses;
    }
    // ConnectivityManager.TYPE_WIFI;ConnectivityManager.TYPE_MOBILE
    public static int checkNetworkConnection(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

        if (activeInfo != null && activeInfo.isConnected()) {
            return activeInfo.getType();
        } else {
            return -1;// ConnectivityManager.TYPE_NONE;
        }
    }

}
