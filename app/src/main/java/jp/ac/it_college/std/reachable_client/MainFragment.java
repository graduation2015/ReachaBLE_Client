package jp.ac.it_college.std.reachable_client;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.ac.it_college.std.reachable_client.json.CouponController;
import jp.ac.it_college.std.reachable_client.json.CouponInfo;
import jp.ac.it_college.std.reachable_client.json.JsonDataReader;
import jp.ac.it_college.std.reachable_client.json.JsonManager;

public class MainFragment extends Fragment implements View.OnClickListener
        , SwipeRefreshLayout.OnRefreshListener{

    public static String IMAGE_PATH;
    public static String JSON_PATH;

    private String checkedCategory;
    private JsonManager jsonManager;
    private List<String> saveList = new ArrayList<>();
    private ChoiceDialog singleChoiceDialog;
    private ToggleButton serviceToggle;
    private LinearLayout cardLinear;
    private LayoutInflater inflater;
    private List<String> list = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView searchView;
    private SwitchCompat bleServiceToggle;
    private RecyclerViewAdapter recyclerViewAdapter;
    private ToggleButton viewToggle;


    public static boolean detailDialogFlag = true;

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
        setHasOptionsMenu(true);

//        setListAdapter(new S3DownloadsListAdapter(getActivity(), R.layout.row_s3_downloads, items));

        this.inflater = inflater;

        Collections.addAll(list, new File(IMAGE_PATH).list());
        Collections.reverse(list);
        saveList.addAll(list);

        View contentView = inflater.inflate(R.layout.fragment_main, container, false);

        cardLinear = (LinearLayout) contentView.findViewById(R.id.cardLinear);

        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.my_recycler_view);
        // コンテンツの変化でRecyclerViewのサイズが変わらない場合は、
        // パフォーマンスを向上させることができる
        mRecyclerView.setHasFixedSize(true);
        setSearchView(list);
        setDownLoads(list);
        updateInfoJson(list);

        jsonManager = new JsonManager(getActivity());

        singleChoiceDialog = ChoiceDialog.newInstance(this, new CategorySingleChoiceDialog());

        findViews(contentView);
        return contentView;
    }

    private void findViews(View contentView) {
/*        contentView.findViewById(R.id.filter_btn).setOnClickListener(this);
        contentView.findViewById(R.id.update_btn).setOnClickListener(this);
        serviceToggle = (ToggleButton) contentView.findViewById(R.id.service_toggle);
        serviceToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleServiceToggle();
            }
        });*/
/*        serviceToggle = (ToggleButton) contentView.findViewById(R.id.service_toggle);
        serviceToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceToggle.isChecked()) {
                    mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                            2, StaggeredGridLayoutManager.VERTICAL));
                } else {
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                }
            }
        });*/
        mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.contentView);

        // 色設定
/*        mSwipeRefreshLayout.setColorSchemeResources(R.color.red,
                R.color.green, R.color.blue,
                R.color.orange);*/
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(this);


    }

/*
    public EmptySupportRecyclerView getCouponListView() {
        if (mCouponListView == null) {
            mCouponListView = (EmptySupportRecyclerView) getContentView().findViewById(R.id.coupon_list);
            //リストが空の際に表示するViewをセット
            mCouponListView.setEmptyView(getEmptyView());
        }
        return mCouponListView;
    }
*/

    private void setSearchView(List<String> list) {
        cardLinear.removeAllViews();

        // LinearLayoutManagerを使用する
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // アダプタを指定する
        mAdapter = new RecyclerViewAdapter(getActivity(), list);
        recyclerViewAdapter = (RecyclerViewAdapter) mAdapter;
        recyclerViewAdapter.setOnClickCardViewListener(new RecyclerViewAdapter.OnClickCardView() {
            @Override
            public void exec(String key) {
                if (CouponDetailsDialog.dialogFlag) {
                    searchView.onActionViewCollapsed();
                    CouponDetailsDialog.dialogFlag = false;
                    int index = recyclerViewAdapter.getPosition(key);
                    CouponDetailsDialog dialog = new CouponDetailsDialog().newInstance(getActivity(), key, index);
                    dialog.show(getFragmentManager(), "CouponDetailDialog");

                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.setAdapter(recyclerViewAdapter);
    }

    /**
     * ダウンロードされた画像をリストに追加して表示
     * @param list
     */
    private void setDownLoads(List<String> list) {
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 更新時にリストを更新し、新しくダウンロードした画像を表示
     */
    private void updateList() {
        list = new ArrayList<>();
        Collections.addAll(list, new File(IMAGE_PATH).list());
        Collections.reverse(list);
        recyclerViewAdapter.addList(list);
        searchView.onActionViewCollapsed();
        mAdapter.notifyDataSetChanged();
        updateInfoJson(list);
    }

    private boolean searchCoupon(String searchKey) {
        List<CouponInfo> couponInfoList = jsonManager.getCouponInfoList();
        String[] queryArray = searchKey.split(" ", 0);
        recyclerViewAdapter.itemReset();
        for (String name : list) {
            CouponInfo couponInfo = couponInfoList.get(list.indexOf(name));
            for (String queryKey : queryArray) {
                if (recyclerViewAdapter.containsVisible(name) && !couponInfo.getMetaData().contains(queryKey)) {
                    recyclerViewAdapter.searchResult(name);
                }
            }
        }

        mAdapter.notifyDataSetChanged();
//        list = tmpList;
        searchView.clearFocus();
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_toolbar, menu);

        final MenuItem searchMenu = menu.findItem(R.id.menu_search);
        searchView = (SearchView) searchMenu.getActionView();
//        MenuItemCompat.setOnActionExpandListener(item, this);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return searchCoupon(query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
/*        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //SearchViewからフォーカスが外れた際にメニューを閉じる
                    searchView.onActionViewCollapsed();
                }
            }
        });*/

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                updateList();
                Log.v("test", "update");
                return false;
            }
        });


        final MenuItem menuBleToggle = menu.findItem(R.id.menu_switch);
        bleServiceToggle = (SwitchCompat) menuBleToggle.getActionView();
        bleServiceToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                bleServiceToggle.setClickable(false);
                if (isChecked && bleServiceToggle.getLinksClickable()) {
                    Intent intent = new Intent(getActivity(), DownloadService.class);
                    getActivity().startService(intent);
                } else {
                    new BleDeviceListManager().resetList();
                    getActivity().stopService(new Intent(getActivity(), DownloadService.class));
//                    bluetoothDisable();
                }
            }
        });
        checkServiceRunning(getActivity(), DownloadService.class);

        final MenuItem menuViewToggle = menu.findItem(R.id.menu_view_toggle);
        menuViewToggle.setActionView(R.layout.menu_view_toggle_layout);
        viewToggle = (ToggleButton) menuViewToggle.getActionView();
        viewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    menuViewToggle.setIcon(R.drawable.ic_view_list_black_24dp);
                    mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                            2, StaggeredGridLayoutManager.VERTICAL));
                } else {
                    menuViewToggle.setIcon(R.drawable.ic_view_quilt_black_24dp);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                }
            }
        });
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
/*            case R.id.filter_btn:
                showCategorySingleChoiceDialog();
                break;
            case R.id.update_btn:
                updateList();
                break;*/
            default:
                break;
        }
    }

/*
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String key = list.get((int) id);
        if(listener != null) {
            listener.goToCouponDetails(key, list.indexOf(key));
        }

    }
*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CategorySingleChoiceDialog.REQUEST_ITEMS) {
            switch (resultCode) {
                case DialogInterface.BUTTON_POSITIVE:
                    setCategories(data);
                    if (checkedCategory.equals("All")) {
//                        saveList = list;
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
/*    private void bleServiceToggle() {
        if (serviceToggle.isChecked()) {
            Intent intent = new Intent(getActivity(), DownloadService.class);
            getActivity().startService(intent);
        } else {
            new BleDeviceListManager().resetList();
            getActivity().stopService(new Intent(getActivity(), DownloadService.class));
            bluetoothDisable();
        }
    }*/

    /**
     * Activity起動時または更新ボタンを押されたときにダウンロードされたjsonファイルを読み込んでinfo.jsonファイルにまとめる
     * @param list
     */
    private void updateInfoJson(List<String> list) {
        CouponController controller = new CouponController();
        try {
            controller.checkCouponDate(getActivity(), list);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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
//        executeCategoryFilter(rootObject);
    }

    /**
     * フィルターを実行してリストを更新
     * @param rootObject
     */
/*    private void executeCategoryFilter(JSONObject rootObject) {
        saveList.clear();
//        jsonManager.executeFilter(rootObject, getFilter(), CouponInfo.CATEGORY, items);
        jsonManager.executeFilter(
                rootObject, getCheckedCategory(), CouponInfo.CATEGORY, saveList);

        setDownLoads(saveList);
    }*/

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
//            serviceToggle.setChecked(true);
            bleServiceToggle.setChecked(true);
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

    @Override
    public void onRefresh() {
        updateList();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 更新が終了したらインジケータ非表示
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 500);
    }
}