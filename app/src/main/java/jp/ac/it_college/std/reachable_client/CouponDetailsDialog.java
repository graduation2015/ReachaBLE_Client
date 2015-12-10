package jp.ac.it_college.std.reachable_client;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.ac.it_college.std.reachable_client.json.CouponInfo;
import jp.ac.it_college.std.reachable_client.json.JsonManager;

public class CouponDetailsDialog extends Dialog{
    //Scrollview
    private TextView company_label;
    private TextView address_label;
    private Button button;

    private ImageView imageView;

    public CouponDetailsDialog(Context context, String key, int index) {
        super(context);

        setContentView(R.layout.coupon_details_dialog);

        JsonManager manager = new JsonManager(context);
        List<CouponInfo> list = manager.getCouponInfoList();
        CouponInfo couponInfo = list.get(index);

//        View view = inflater.inflate(R.layout.fragment_coupon_details, container, false );

        company_label = (TextView) findViewById(R.id.company_name);
        address_label = (TextView) findViewById(R.id.address);

        company_label.setText(couponInfo.getKey());
        address_label.setText(couponInfo.getAddress());

        imageView = (ImageView) findViewById(R.id.coupon_img);

        Bitmap bitmap = BitmapFactory.decodeFile(MainFragment.IMAGE_PATH + "/" + key);
        imageView.setImageBitmap(bitmap);

        button = (Button) findViewById(R.id.ok_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
