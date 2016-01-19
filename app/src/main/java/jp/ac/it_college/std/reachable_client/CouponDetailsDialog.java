package jp.ac.it_college.std.reachable_client;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.ac.it_college.std.reachable_client.json.CouponInfo;
import jp.ac.it_college.std.reachable_client.json.JsonManager;


public class CouponDetailsDialog extends DialogFragment {
    public static final String KEY = "key";
    public static final String INDEX = "index";
    public static boolean dialogFlag = true;

    public static CouponDetailsDialog newInstance(String key, int index){
        CouponDetailsDialog dialogFragment = new CouponDetailsDialog();

        Bundle bundle = new Bundle();
        bundle.putString(KEY, key);
        bundle.putInt(INDEX, index);

        dialogFragment.setArguments(bundle);

        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity());
        // タイトル非表示
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // フルスクリーン
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.coupon_details_dialog);

        // 背景を透明にする
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //クーポン詳細設定
        Bundle args = getArguments();
        JsonManager manager = new JsonManager(getActivity());
        List<CouponInfo> list = manager.getCouponInfoList();
        CouponInfo couponInfo = list.get(args.getInt(INDEX));

        TextView company_label = (TextView) dialog.findViewById(R.id.company_name);
        TextView address_label = (TextView) dialog.findViewById(R.id.address);
        TextView description_label = (TextView) dialog.findViewById(R.id.description);

        company_label.setText(couponInfo.getKey());
        address_label.setText(couponInfo.getAddress());
        description_label.setText(couponInfo.getDescription());

        ImageView imageView = (ImageView) dialog.findViewById(R.id.coupon_img);

        Bitmap bitmap = BitmapFactory.decodeFile(MainFragment.IMAGE_PATH + "/" + args.getString(KEY));
        imageView.setImageBitmap(bitmap);

        // OK ボタンのリスナ
        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainFragment.detailDialogFlag = !MainFragment.detailDialogFlag;
                dialogFlag = true;
                dismiss();
            }
        });

        return dialog;
    }

    /**
     * 受け取った引数で画像の詳細を当てはめる
     *
     */
/*
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.coupon_details_dialog, container, false);

        JsonManager manager = new JsonManager(context);
        List<CouponInfo> list = manager.getCouponInfoList();
        CouponInfo couponInfo = list.get(index);

//        View view = inflater.inflate(R.layout.fragment_coupon_details, container, false );

        company_label = (TextView) view.findViewById(R.id.company_name);
        address_label = (TextView) view.findViewById(R.id.address);
        description_label = (TextView) view.findViewById(R.id.description);

        company_label.setText(couponInfo.getKey());
        address_label.setText(couponInfo.getAddress());
        description_label.setText(couponInfo.getDescription());

        imageView = (ImageView) view.findViewById(R.id.coupon_img);

        Bitmap bitmap = BitmapFactory.decodeFile(MainFragment.IMAGE_PATH + "/" + key);
        imageView.setImageBitmap(bitmap);

        button = (Button) view.findViewById(R.id.ok_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }*/
}
