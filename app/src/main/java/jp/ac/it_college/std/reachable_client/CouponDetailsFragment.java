package jp.ac.it_college.std.reachable_client;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.ac.it_college.std.reachable_client.json.CouponInfo;
import jp.ac.it_college.std.reachable_client.json.JsonManager;


public class CouponDetailsFragment extends Fragment {

    //Scrollview
    private TextView company_label;
    private TextView address_label;


    private ImageView imageView;
    private OnFragmentInteractionListener mListener;

    public static CouponDetailsFragment newInstance(String param1, String param2) {
        CouponDetailsFragment fragment = new CouponDetailsFragment();
        return fragment;
    }

    public CouponDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        JsonManager manager = new JsonManager(getActivity());
        List<CouponInfo> list = manager.getCouponInfoList();
        CouponInfo couponInfo = list.get(getArguments().getInt("index"));

        View view = inflater.inflate(R.layout.fragment_coupon_details, container, false );

        company_label = (TextView) view.findViewById(R.id.company_name);
        address_label = (TextView) view.findViewById(R.id.address);

        company_label.setText(couponInfo.getKey());
        address_label.setText(couponInfo.getAddress());

        imageView = (ImageView) view.findViewById(R.id.coupon_img);

        String key = getArguments().getString("key");
        Bitmap bitmap = BitmapFactory.decodeFile(MainFragment.IMAGE_PATH + "/" + key);
        imageView.setImageBitmap(bitmap);

        view.invalidate();

        // Inflate the layout for this fragment
        return view;
    }

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction();
    }


}
