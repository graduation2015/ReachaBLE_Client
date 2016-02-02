package jp.ac.it_college.std.reachable_client.json;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.ac.it_college.std.reachable_client.MainFragment;

public class CouponController {

    public static final String PREF_KEY = "downloadDate";

    public void addCouponDownloadDate(Context context, String key) throws FileNotFoundException, JSONException {
        SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        //現在の時刻をLong型で取得(1970年1月1日からのミリ秒)
        editor.putLong(key, System.currentTimeMillis());
        editor.apply();
    }

    public void checkCouponDate(Context context, List<String> list) throws FileNotFoundException {
        SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE);
        JsonManager manager = new JsonManager(context);

        //1000 * 60 * 60 * 24(24時間経過したか)
        long currentTime = System.currentTimeMillis();
        for (String key : list) {
            if (currentTime - pref.getLong(key, currentTime) > 1000 * 60 * 60 * 24) {
                //クーポンのjsonファイルと画像を削除
                File jsonFile = new File(MainFragment.JSON_PATH + "/" + key + ".json");
                jsonFile.delete();
                File couponImage = new File(MainFragment.IMAGE_PATH + "/" + key);
                couponImage.delete();

                JSONObject object = manager.getJsonRootObject();
                object.remove(key);
                manager.putJsonObj(object);
            }
        }
    }
}
