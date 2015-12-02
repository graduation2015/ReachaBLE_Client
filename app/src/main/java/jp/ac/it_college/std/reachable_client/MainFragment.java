package jp.ac.it_college.std.reachable_client;

import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        setDownLoads(list);

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
    }

    private void setDownLoads(List<String> list) {
        items.clear();

        for (String name : list) {
            Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_PATH + "/" + name);
            items.add(bitmap);
        }
        ((S3DownloadsListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private void showCategorySingleChoiceDialog() {
        singleChoiceDialog.show(getFragmentManager(), "singleChoice");
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
                getActivity().stopService(new Intent(getActivity(), DownloadService.class));
                break;
            default:
                break;
        }
    }

    private String getCheckedCategory() {
        return checkedCategory;
    }
}