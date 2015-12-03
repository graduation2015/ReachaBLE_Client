package jp.ac.it_college.std.reachable_client.json;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class JsonManager {

    private static final String FILE_NAME = "info.json";
    public static final String TAG = "JsonManager";

    private Context context;
    private File file;
    private JsonDataWriter jsonDataWriter;
    private JsonDataSelector jsonDataSelector;
    private JsonDataReader jsonDataReader;
    private JsonDataRemover jsonDataRemover;

    public JsonManager(Context context) {
        this.context = context;

        File dir = createExternalStorageDir(context, Environment.DIRECTORY_DOCUMENTS);
        file = new File(dir, FILE_NAME);

        jsonDataWriter = new JsonDataWriter();
        jsonDataSelector = new JsonDataSelector();
        jsonDataReader = new JsonDataReader();
        jsonDataRemover = new JsonDataRemover();

        // jsonファイルがない場合作る
        if (!file.exists()) {
            try {
                jsonDataWriter.initJsonObj(file);
                Toast.makeText(context, "JSON file was created", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public File getFile() {
        return file;
    }

    /**
     * JSONオブジェクトを返す
     *
     * @return
     */
    public JSONObject getJsonRootObject() {
        JSONObject jsonObject = null;

        try {
            InputStream is = new FileInputStream(getFile());
            jsonObject = new JSONObject(jsonDataReader.getJsonStr(is));
        } catch (JSONException | FileNotFoundException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * 外部ストレージにディレクトリを作成する
     *
     * @param context
     * @param dirType Environmentのディレクトリータイプ
     * @return
     */
    private File createExternalStorageDir(Context context, String dirType) {
        File file = new File(context.getExternalFilesDir(dirType).getPath());

        if (!file.mkdirs()) {
//            Toast.makeText(context, "File already exists", Toast.LENGTH_SHORT).show();
        }

        return file;
    }

    /**
     * 読み書き可能な外部ストレージをチェック
     *
     * @return
     */
    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 少なくとも読み取りは可能な外部ストレージか、チェック
     *
     * @return
     */
    private boolean isExternalStorageReadable() {
        return isExternalStorageWritable()
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    public void putJsonObj(JSONObject rootObj, CouponInfo info) {
        try {
            FileOutputStream outputStream = new FileOutputStream(getFile(), false);
            jsonDataWriter.writeJson(outputStream, rootObj, info);
            outputStream.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public void putJsonObj(JSONObject changedObj) {
        try {
            FileOutputStream outputStream = new FileOutputStream(getFile(), false);
            jsonDataWriter.writeJson(outputStream, changedObj);
            outputStream.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
    //リスト用
    public void executeListFilter(
            JSONObject rootObject, List<String> filters, String target, List<String> items) {
        jsonDataSelector.executeFilter(rootObject, filters, target, items);
    }

    public void executeFilter(
            JSONObject rootObject, String filter, String target, List<String> items) {
        jsonDataSelector.executeFilter(rootObject, filter, target, items);
    }

    /**
     * JSONオブジェクトからCouponInfoのリストを取得
     * @return
     */
    public List<CouponInfo> getCouponInfoList() {
        return jsonDataSelector.getCouponInfoList(getJsonRootObject());
    }

    /**
     * 指定されたキーのオブジェクトを削除
     * @param key
     */
    public void removeObject(String key) {
        JSONObject removedObject = jsonDataRemover.remove(getJsonRootObject(), key);
        try {
            FileOutputStream outputStream = new FileOutputStream(getFile(), false);
            jsonDataWriter.writeJson(outputStream, removedObject);
            outputStream.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }
}