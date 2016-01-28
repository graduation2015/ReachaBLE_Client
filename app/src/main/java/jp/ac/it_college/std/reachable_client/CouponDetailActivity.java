package jp.ac.it_college.std.reachable_client;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Collections;

import jp.ac.it_college.std.reachable_client.json.CouponInfo;
import jp.ac.it_college.std.reachable_client.json.JsonManager;

public class CouponDetailActivity extends AppCompatActivity {

    public static final String SELECTED_ITEM = "selected:item";
    public static final String SELECTED_ITEM_POSITION = "selected:position";

    /* Views */
    private ImageView mHeaderImageView;
    private Toolbar mToolbar;
    private TextView mCompanyName;
    private TextView mAddress;
    private TextView mDescriptionView;
    private TextView mCategoryView;
    private CoordinatorLayout mCoordinatorLayout;
    private TextView mTitleView;

    /* Coupon */
    private CouponInfo mSelectedItem;

    /* Json */
    private JsonManager mJsonManager;

    /* IMAGE */
    public static final int IMG_THUMBNAIL_WIDTH = 1024;
    public static final int IMG_THUMBNAIL_HEIGHT = 576;
    public static final int IMG_FULL_SIZE_WIDTH = 1984;
    public static final int IMG_FULL_SIZE_HEIGHT = 1116;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_detail);

        initSettings();
    }

    private void initSettings() {
        loadItem(getSelectedItem());
        setUpActionBar(getToolbar());

        //JsonManagerのインスタンスを生成
        mJsonManager = new JsonManager(this);

    }


    /**
     * Actionbarの初期設定を実行する
     * @param toolbar Actionbarにセットするツールバー
     */
    private void setUpActionBar(Toolbar toolbar) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //アクションバーの戻るボタン押下時の処理
                finishAfterTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * クーポン情報を読み込む
     */
    private void loadItem(CouponInfo info) {
        //Company Name set
        getCompanyNameView().setText(info.getCompanyName());

        //住所をセット
        getAddressView().setText(info.getAddress());

        //説明をセット
        getDescriptionView().setText(info.getDescription());

        //カテゴリーをセット
        String category = "";
        for (String tag : info.getCategory()) {
            category += tag + ", ";
        }
        getCategoryView().setText(category.length() == 0
                ? getString(R.string.no_category) : category.substring(0, category.length() - 2));
        //タイトルをセット
        getTitleView().setText(info.getTitle());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && addTransitionListener()) {
            loadThumbnail();
        } else {
            loadFullSizeImage();
        }
    }

    /**
     * サムネイル画像を読み込む
     */
    private void loadThumbnail() {
        Picasso.with(getHeaderImageView().getContext())
                .load(new File(getSelectedItem().getFilePath()))
                .transform(new BitmapTransform(
                        IMG_THUMBNAIL_WIDTH, IMG_THUMBNAIL_HEIGHT))
                .noFade()
                .noPlaceholder()
                .into(getHeaderImageView());
    }

    /**
     * フルサイズの画像を読み込む
     */
    private void loadFullSizeImage() {
        Picasso.with(getHeaderImageView().getContext())
                .load(new File(getSelectedItem().getFilePath()))
                .transform(new BitmapTransform(
                        IMG_FULL_SIZE_WIDTH, IMG_FULL_SIZE_HEIGHT))
                .noFade()
                .noPlaceholder()
                .into(getHeaderImageView());
    }

    /**
     * TransitionListerを登録する
     * @return 正常に処理が完了した場合はtrueを返す
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean addTransitionListener() {
        final Transition transition = getWindow().getSharedElementEnterTransition();

        if (transition != null) {
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    loadFullSizeImage();

                    transition.removeListener(this);
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });

            return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public ImageView getHeaderImageView() {
        if (mHeaderImageView == null) {
            mHeaderImageView = (ImageView) findViewById(R.id.img_coupon_pic);
        }
        return mHeaderImageView;
    }

    public CouponInfo getSelectedItem() {
        if (mSelectedItem == null) {
            mSelectedItem = (CouponInfo) getIntent().getSerializableExtra(SELECTED_ITEM);
        }
        return mSelectedItem;
    }

    public Toolbar getToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        }
        return mToolbar;
    }

    public TextView getCompanyNameView() {
        if(mCompanyName == null) {
            mCompanyName = (TextView) findViewById(R.id.company_name);
        }
        return mCompanyName;
    }

    public TextView getAddressView() {
        if(mAddress == null) {
            mAddress = (TextView) findViewById(R.id.address);
        }
        return mAddress;
    }

    public TextView getDescriptionView() {
        if (mDescriptionView == null) {
            mDescriptionView = (TextView) findViewById(R.id.description);
        }
        return mDescriptionView;
    }

    public TextView getCategoryView() {
        if (mCategoryView == null) {
            mCategoryView = (TextView) findViewById(R.id.categorys);
        }
        return mCategoryView;
    }

    public TextView getTitleView() {
        if (mTitleView == null) {
            mTitleView = (TextView) findViewById(R.id.coupon_title);
        }
        return mTitleView;
    }


}
