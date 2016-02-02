package jp.ac.it_college.std.reachable_client.json;

import org.json.JSONObject;

public class JsonDataRemover {

    //Jsonデータを削除
    public JSONObject remove(JSONObject rootObj, String key) {
        rootObj.remove(key);

        return rootObj;
    }
}