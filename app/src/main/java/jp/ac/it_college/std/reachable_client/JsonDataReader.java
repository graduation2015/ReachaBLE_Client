package jp.ac.it_college.std.reachable_client;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class JsonDataReader {
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * JSONファイルを読み込み文字列で返す
     *
     * @param is
     * @return
     */
    protected String getJsonStr(InputStream is) {
        String json = null;
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, DEFAULT_ENCODING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

}