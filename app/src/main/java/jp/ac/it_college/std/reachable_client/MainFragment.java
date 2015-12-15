package jp.ac.it_college.std.reachable_client;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.amazonaws.mobileconnectors.s3.transfermanager.Download;

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
    private ToggleButton serviceToggle;

//    public static final String TAGS_PATH;
    private List<String> list;

    //Fragmentで受け取ったイベントをActivityへ投げる
    private ChangeFragmentListener listener = null;
    public interface ChangeFragmentListener {
        void goToCouponDetails(String key, int index);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof ChangeFragmentListener)) {
            throw new UnsupportedOperationException(
                    "Listener is not Implementation.");
        } else {
            listener = (ChangeFragmentListener) activity;
        }
    }

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

        checkServiceRunning(getActivity(), DownloadService.class);

        return contentView;
    }

    private void findViews(View contentView) {
        contentView.findViewById(R.id.filter_btn).setOnClickListener(this);
        contentView.findViewById(R.id.update_btn).setOnClickListener(this);
        serviceToggle = (ToggleButton) contentView.findViewById(R.id.service_toggle);
        serviceToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleServiceToggle();
            }
        });
    }

    /**
     * ダウンロードされた画像をリストに追加して表示
     * @param list
     */
    private void setDownLoads(List<String> list) {
        items.clear();

        for (String name : list) {
            Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_PATH + "/" + name);
            items.add(bitmap);
        }
        ((S3DownloadsListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    /**
     * 更新ボタンを押された時にリストを更新し、新しくダウンロードした画像を表示
     */
    private void updateList() {
        list = Arrays.asList(new File(IMAGE_PATH).list());
        Collections.reverse(list);
        setDownLoads(list);
        updateInfoJson(list);
    }

    /**
     * 絞り込みボタンを押された時にカテゴリー選択のダイアログを表示
     */
    private void showCategorySingleChoiceDialog() {
        singleChoiceDialog.show(getFragmentManager(), "singleChoice");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.filter_btn:
                showCategorySingleChoiceDialog();
                break;
            case R.id.update_btn:
                updateList();
                break;
            default:
                break;
        }
    }

    /**
     * リストに表示されている画像をタップするとActivityにイベントを投げて詳細のダイアログを表示する
     * @param l
     * @param v
     * @param position 上から何番目の画像が押されたかを取得
     * @param id
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String key = list.get((int) id);
        if(listener != null) {
            listener.goToCouponDetails(key, list.indexOf(key));
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
                        filterItems = list;
                        setDownLoads(list);
                    } else {
                        loadJSON();
                    }
                    break;
            }
        }
    }

    /**
     * トグルボタンが押された時の状態を受け取ってDownloadServiceを起動または停止する
     */
    private void bleServiceToggle() {
        if (serviceToggle.isChecked()) {
            Intent intent = new Intent(getActivity(), DownloadService.class);
            getActivity().startService(intent);
        } else {
            new BleDeviceListManager().resetList();
            getActivity().stopService(new Intent(getActivity(), DownloadService.class));
            bluetoothDisable();
        }
    }

    /**
     * Activity起動時または更新ボタンを押されたときにダウンロードされたjsonファイルを読み込んでinfo.jsonファイルにまとめる
     * @param list
     */
    private void updateInfoJson(List<String> list) {
        for (String key : list) {
            String jsonStr;
            File downloadJsonPath = new File(MainFragment.JSON_PATH, key + ".json");

            try {
                JsonDataReader reader = new JsonDataReader();
                jsonStr = reader.getJsonStr(new FileInputStream(downloadJsonPath));

                JSONObject downloadJson = new JSONObject(jsonStr).getJSONObject(key);

                jsonManager = new JsonManager(getActivity());
                JSONObject rootJson = jsonManager.getJsonRootObject().put(key, downloadJson);

                jsonManager.putJsonObj(rootJson);
            } catch (JSONException | FileNotFoundException e) {
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

    /**
     * 端末側のBluetoothをOFFにする
     */
    private void bluetoothDisable() {
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();

        if (bt == null) {
            return;
        }

        if (bt.isEnabled()) {
            bt.disable();
        }
    }

    /**
     * 初回起動時に必要なディレクトリを作成
     */
    private void mkdir() {

   /*     File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());
        file.mkdirs();*/
        IMAGE_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
        JSON_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
        new File(IMAGE_PATH).mkdirs();
        new File(JSON_PATH).mkdirs();

    }

    /**
     * 絞り込みで選択したカテゴリーを返す
     * @return
     */
    private String getCheckedCategory() {
        return checkedCategory;
    }

    /**
     * Activity起動時DownloadServiceが動いていればトグルボタンの状態をONにする
     * @param c　context
     * @param cls 確認したいserviceクラス、ここでいうDownloadService
     */
    private void checkServiceRunning(Context c, Class<?> cls) {
        if (isServiceRunning(c, cls)) {
            serviceToggle.setChecked(true);
        }
    }

    /**
     * serviceクラスが動いているか判別してboolean型を返す
     * @param c　context
     * @param cls 確認したいserviceクラス、ここでいうDownloadService
     * @return serviceが動いていればtrue、動いてなければfalseを返す
     */
    public boolean isServiceRunning(Context c, Class<?> cls) {
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningService = am.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo i : runningService) {
            Log.d("test", "service: " + i.service.getClassName() + " : " + i.started);
            if (cls.getName().equals(i.service.getClassName())) {
                Log.d("test", "running");
                return true;
            }
        }
        return false;
    }
}