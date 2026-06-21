package com.ccs.javadroid.git;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.eclipse.jgit.transport.URIish;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Зберігає Git-токени per-host у SharedPreferences з простим обфускацією XOR.
 * Це не захищає від руткітів, але приховує токени від випадкового перегляду
 * та запобігає текстовому витоку у бекапах.
 */
public final class GitCredentialsStore {

    private static final String PREFS = "com.ccs.javadroid.git";
    private static final String KEY_AUTHOR_NAME  = "author_name";
    private static final String KEY_AUTHOR_EMAIL = "author_email";

    // Salt для обфускації — стабільний на пристрої.
    private final byte[] salt;
    private final SharedPreferences prefs;

    public GitCredentialsStore(Context ctx) {
        this.prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String installId = ctx.getPackageName() + ":javadroid-git-creds-v1";
        this.salt = sha256(installId.getBytes(StandardCharsets.UTF_8));
    }

    public void saveAuthor(String name, String email) {
        prefs.edit()
                .putString(KEY_AUTHOR_NAME, name == null ? "" : name)
                .putString(KEY_AUTHOR_EMAIL, email == null ? "" : email)
                .apply();
    }

    public String authorName()  { return prefs.getString(KEY_AUTHOR_NAME, ""); }
    public String authorEmail() { return prefs.getString(KEY_AUTHOR_EMAIL, ""); }

    public void save(String url, String username, String token) {
        String host = hostOf(url);
        prefs.edit()
                .putString("u_" + host, username == null ? "" : username)
                .putString("t_" + host, encrypt(token))
                .apply();
    }

    public String username(String url) {
        return prefs.getString("u_" + hostOf(url), "");
    }

    public String token(String url) {
        return decrypt(prefs.getString("t_" + hostOf(url), ""));
    }

    public boolean hasCredentials(String url) {
        String h = hostOf(url);
        return prefs.contains("u_" + h) && prefs.contains("t_" + h);
    }

    public void clear(String url) {
        String h = hostOf(url);
        prefs.edit().remove("u_" + h).remove("t_" + h).apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    // ── Helpers ───────────────────────────────────────────────

    private static String hostOf(String url) {
        if (url == null) return "default";
        try {
            URIish u = new URIish(url.trim());
            String host = u.getHost();
            return host == null || host.isEmpty() ? "default" : host;
        } catch (Exception e) {
            return "default";
        }
    }

    private String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) return "";
        byte[] data = plain.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = (byte) (data[i] ^ salt[i % salt.length]);
        }
        return Base64.encodeToString(out, Base64.NO_WRAP);
    }

    private String decrypt(String b64) {
        if (b64 == null || b64.isEmpty()) return "";
        try {
            byte[] data = Base64.decode(b64, Base64.NO_WRAP);
            byte[] out = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                out[i] = (byte) (data[i] ^ salt[i % salt.length]);
            }
            return new String(out, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (Exception e) {
            return new byte[]{1, 2, 3, 4};
        }
    }
}
