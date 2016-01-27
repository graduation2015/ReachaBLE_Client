package jp.ac.it_college.std.reachable_client;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
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
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
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

    private JsonManager jsonManager;
    private List<String> saveList = new ArrayList<>();
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
    private List<CouponInfo> couponInfoList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mkdir();
        setHasOptionsMenu(true);

        this.inflater = inflater;

        Collections.addAll(list, new File(IMAGE_PATH).list());
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
        couponInfoList = jsonManager.getCouponInfoList();

        findViews(contentView);
        return contentView;
    }

    private void findViews(View contentView) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.contentView);

        // 色設定

        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(this);


    }

    private ActivityOptionsCompat makeSharedElementOptions(View view) {
        View toolbar = ((MainActivity) getActivity()).getToolbar();
        View image = view.findViewById(R.id.img);
        View title = view.findViewById(R.id.coupon_title_label);
        View companyName = view.findViewById(R.id.company_name_label);

        return ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                new Pair<View, String>(toolbar, getString(R.string.transition_toolbar)),
                new Pair<View, String>(image, getString(R.string.transition_img)),
                new Pair<View, String>(title, getString(R.string.transition_title)),
                new Pair<View, String>(companyName, getString(R.string.transition_company_name))
        );
    }
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
            public void exec(int index, View view) {
                Intent intent = new Intent(getActivity(), CouponDetailActivity.class);
                intent.putExtra(CouponDetailActivity.SELECTED_ITEM, (Serializable) couponInfoList.get(index));
                intent.putExtra(CouponDetailActivity.SELECTED_ITEM_POSITION, index);
                ActivityCompat.startActivity(getActivity(), intent, makeSharedElementOptions(view).toBundle());

            }
        });
        mRecyclerView.setAdapter(mAdapter);
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
//        Collections.reverse(list);
        recyclerViewAdapter.addList(list);
        searchView.onActionViewCollapsed();
        mAdapter.notifyDataSetChanged();
        updateInfoJson(list);
    }

    private boolean searchCoupon(String searchKey) {
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
        searchView.clearFocus();
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_toolbar, menu);

        final MenuItem searchMenu = menu.findItem(R.id.menu_search);
        searchView = (SearchView) searchMenu.getActionView();
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
                if (isChecked && bleServiceToggle.getLinksClickable()) {
                    Intent intent = new Intent(getActivity(), DownloadService.class);
                    getActivity().startService(intent);
                } else {
                    new BleDeviceListManager().resetList();
                    getActivity().stopService(new Intent(getActivity(), DownloadService.class));
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

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
     * 初回起動時に必要なディレクトリを作成
     */
    private void mkdir() {

        IMAGE_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
        JSON_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
        new File(IMAGE_PATH).mkdirs();
        new File(JSON_PATH).mkdirs();

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