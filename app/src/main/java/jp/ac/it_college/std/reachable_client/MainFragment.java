package jp.ac.it_college.std.reachable_client;

import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jp.ac.it_college.std.reachable_client.aws.S3DownloadsListAdapter;
import jp.ac.it_college.std.reachable_client.json.CouponInfo;
import jp.ac.it_college.std.reachable_client.json.JsonDataReader;
import jp.ac.it_college.std.reachable_client.json.JsonManager;

public class MainFragment extends ListFragment implements View.OnClickListener{

    private List<Bitmap> items = new ArrayList<>();
    public static String IMAGE_PATH;
    public static String JSON_PATH;
    private String checkedCategory;
    private JsonManager jsonManager;
    private List<String> filterItems = new ArrayList<>();
    private List<String> checkedCategories = new ArrayList<>();
    private ChoiceDialog singleChoiceDialog;

//    public static final String TAGS_PATH;
    private List<String> list;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mkdir();

        setListAdapter(new S3DownloadsListAdapter(getActivity(), R.layout.row_s3_downloads, items));
        list = Arrays.asList(new File(IMAGE_PATH).list());
        Collections.reverse(list);

        setDownLoads(list);
        updateInfoJson(list);

        jsonManager = new JsonManager(getActivity());

        singleChoiceDialog = ChoiceDialog.newInstance(this, new CategorySingleChoiceDialog());


        View contentView = inflater.inflate(R.layout.fragment_main, container, false);
        findViews(contentView);

        return contentView;
    }

    private void findViews(View contentView) {
        contentView.findViewById(R.id.filter_btn).setOnClickListener(this);
        contentView.findViewById(R.id.start_btn).setOnClickListener(this);
        contentView.findViewById(R.id.stop_btn).setOnClickListener(this);
        contentView.findViewById(R.id.update_btn).setOnClickListener(this);
    }

    private void setDownLoads(List<String> list) {
        items.clear();

        for (String name : list) {
            Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_PATH + "/" + name);
            items.add(bitmap);
        }
        ((S3DownloadsListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private void updateList() {
        list = Arrays.asList(new File(IMAGE_PATH).list());
        Collections.reverse(list);
        setDownLoads(list);
        updateInfoJson(list);
    }

    private void showCategorySingleChoiceDialog() {
        singleChoiceDialog.show(getFragmentManager(), "singleChoice");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.filter_btn:
                showCategorySingleChoiceDialog();
                break;
            case R.id.start_btn:
                Intent intent = new Intent(getActivity(), DownloadService.class);
                getActivity().startService(intent);
                break;
            case R.id.stop_btn:
                BleDeviceListManager.deviceList = new ArrayList<>();
                getActivity().stopService(new Intent(getActivity(), DownloadService.class));
                bluetoothDisable();
                break;
            case R.id.update_btn:
                updateList();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CategorySingleChoiceDialog.REQUEST_ITEMS) {
            switch (resultCode) {
                case DialogInterface.BUTTON_POSITIVE:
                    setCategories(data);
                    if (checkedCategory.equals("All")) {
                        setDownLoads(list);
                    } else {
                        loadJSON();
                    }
                    break;
            }
        }
    }

    private void updateInfoJson(List<String> list) {
        for (String key : list) {
            String jsonStr;
            File downloadJsonPath = new File(MainFragment.JSON_PATH, key + ".json");

            try {
                JsonDataReader reader = new JsonDataReader();
                jsonStr = reader.getJsonStr(new FileInputStream(downloadJsonPath));

                JSONObject dwonloadJson = new JSONObject(jsonStr).getJSONObject(key);

                jsonManager = new JsonManager(getActivity());
                JSONObject rootJson = jsonManager.getJsonRootObject().put(key, dwonloadJson);

                jsonManager.putJsonObj(rootJson);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ダイアログで選択したカテゴリをセット
     * @param data
     */
    private void setCategories(Intent data) {
/*
        //Multiple用
        List<String> checkedCategories = data.getStringArrayListExtra(CategoryMultipleChoiceDialog.CHECKED_ITEMS);
        this.checkedCategories.clear();
        this.checkedCategories.addAll(checkedCategories);
*/

        //Single用
        this.checkedCategory = data.getStringExtra(CategorySingleChoiceDialog.CHECKED_ITEMS);
    }

    /**
     * フィルターを実行してJSONを読み込む
     */
    private void loadJSON() {
        JSONObject rootObject = jsonManager.getJsonRootObject();
        executeCategoryFilter(rootObject);
    }

    /**
     * フィルターを実行してリストを更新
     * @param rootObject
     */
    private void executeCategoryFilter(JSONObject rootObject) {
        filterItems.clear();
//        jsonManager.executeFilter(rootObject, getFilter(), CouponInfo.CATEGORY, items);
        jsonManager.executeFilter(
                rootObject, getCheckedCategory(), CouponInfo.CATEGORY, filterItems);

        setDownLoads(filterItems);
    }

    private void bluetoothDisable() {
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();

        if (bt == null) {
            return;
        }

        if (bt.isEnabled()) {
            bt.disable();
        }
    }

    private void mkdir() {

   /*     File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());
        file.mkdirs();*/
        IMAGE_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
        JSON_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
        new File(IMAGE_PATH).mkdirs();
        new File(JSON_PATH).mkdirs();

    }

    private String getCheckedCategory() {
        return checkedCategory;
    }
}