package jp.ac.it_college.std.reachable_client.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jp.ac.it_college.std.reachable_client.MainFragment;

public class JsonDataSelector {

    //SingleChoiceDialog用
    public void executeFilter(JSONObject rootObj, String filter, String target, List<String> items) {
        executeFilter(rootObj, rootObj.names(), filter, target, items);
    }

    //リスト(MultipleChoiceDialog)用
    public void executeFilter(
            JSONObject rootObj, List<String> filters, String target, List<String> items) {
        executeListFilter(rootObj, rootObj.names(), filters, target, items);
    }

    //リスト(MultipleChoiceDialog)用
    private void executeListFilter(JSONObject rootObj, JSONArray names,
                                   List<String> filters, String target, List<String> items) {
        if (rootObj == null || names == null) {
            return;
        }
    }

    /**
     * フィルターを実行後リストにアイテムを追加
     * @param rootObj
     * @param names
     * @param filter
     * @param target
     * @param items
     */
    private void executeFilter(JSONObject rootObj, JSONArray names,
                               String filter, String target, List<String> items) {
        if (rootObj == null || names == null) {
            return;
        }

        for (int i = 0; i < names.length(); i++) {
            JSONArray values = getValues(rootObj, names, i, target); //カテゴリを取得
            for (int j = 0; j < values.length(); j++) {
                // フィルターに入力されたタグが付いているKeyをリストに追加
                if (valueExists(values, j, filter)) {
                    addValue(items, names, i);
                }
            }
        }
    }

    /**
     * カテゴリを取得
     * @param object
     * @param names
     * @param position
     * @param key
     * @return
     */
    private JSONArray getValues(JSONObject object, JSONArray names, int position, String key) {
        JSONArray values = null;
        try {
            values = object.getJSONObject(names.getString(position)).getJSONArray(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return values;
    }

    /**
     * フィルターで指定された値の有無をチェック
     * @param values
     * @param position
     * @param filter
     * @return
     */
    private boolean valueExists(JSONArray values, int position, String filter) {
        try {
            return values.getString(position).equals(filter);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * リストにアイテムを追加
     * @param items
     * @param names
     * @param position
     */
    private void addValue(List<String> items, JSONArray names, int position) {
        try {
            items.add(names.getString(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<CouponInfo> getCouponInfoList(JSONObject rootObj) {
        List<CouponInfo> list = new ArrayList<>();
//        Iterator<String> iterator = rootObj.keys();
        List<String> couponKeys = new ArrayList<>();
        Collections.addAll(couponKeys, new File(MainFragment.IMAGE_PATH).list());

        for (String key : couponKeys) {
            try {
                JSONObject object = rootObj.getJSONObject(key);
                list.add(createCouponInfo(key, object));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return list;
    }

    private CouponInfo createCouponInfo(String key, JSONObject object) {
        String title = null, name = null, address = null, description = null, filePath = null;
        List<String> category = new ArrayList<>();
        try {
            name = object.getString(CouponInfo.NAME);
            title = object.getString(CouponInfo.TITLE);
            address  = object.getString(CouponInfo.ADDRESS);
            description = object.getString(CouponInfo.DESCRIPTION);
            category = createList(object.getJSONArray(CouponInfo.CATEGORY));
            File file = new File(MainFragment.IMAGE_PATH + "/" + key);
            filePath = file.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new CouponInfo(key, title, name, address, description, category, filePath);
    }

    private List<String> createList(JSONArray category) {
        List<String> list = new ArrayList<>();

        for (int i = 0; i < category.length(); i++) {
            try {
                list.add(category.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

}