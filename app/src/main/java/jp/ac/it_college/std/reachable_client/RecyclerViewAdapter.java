package jp.ac.it_college.std.reachable_client;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.ac.it_college.std.reachable_client.json.CouponInfo;
import jp.ac.it_college.std.reachable_client.json.JsonManager;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements View.OnClickListener{
    public static final int THUMBNAIL_WIDTH = 1024;
    public static final int THUMBNAIL_HEIGHT = 576;

    private LayoutInflater mLayoutInflater;
    private List<String> visible = new ArrayList<>();
    private List<String> mDataList = new ArrayList<>();
    private Context context;
    private RecyclerView recyclerView;


    public RecyclerViewAdapter(Context context, List<String> dataList) {
        super();
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
        mDataList = dataList;
        visible.addAll(mDataList);
    }

    public OnClickCardView listener;
    public interface OnClickCardView {
        void exec(String key);
    }
    public void setOnClickCardViewListener(OnClickCardView onClickCardView) {
        listener = onClickCardView;
    }

    // getViewのinfrateするところだけ取り出した感じ
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        v.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemCount() {
        return visible.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        Bitmap bitmap = BitmapFactory.decodeFile(MainFragment.IMAGE_PATH + "/" + visible.get(position));
//        holder.imageView.setImageBitmap(bitmap);

        File file = new File(MainFragment.IMAGE_PATH + "/" + visible.get(position));
        Picasso.with(context).load(file).placeholder(R.drawable.loading)
                .transform(new BitmapTransform(RecyclerViewAdapter.THUMBNAIL_WIDTH, RecyclerViewAdapter.THUMBNAIL_HEIGHT))
                .into(holder.imageView);
        holder.imageView.setTag(R.integer.tag_company_name, visible.get(position));

/*        String data = visible.get(position);
        holder.text.setText(data);*/

        JsonManager manager = new JsonManager(context);
        List<CouponInfo> couponInfoList = manager.getCouponInfoList();
        CouponInfo couponInfo = couponInfoList.get(position);

        holder.text.setText(couponInfo.getTitle());
        holder.text2.setText(couponInfo.getCompanyName());
    }

    @Override
    public void onClick(View v) {
//        TextView text = (TextView) v.findViewById(R.id.company_name_label);
        String index = (String) v.findViewById(R.id.img).getTag(R.integer.tag_company_name);

        if (listener != null) {
            listener.exec(index);
        }
    }

    public void searchResult(String key) {
        visible.remove(key);
    }

    public boolean containsVisible(String key) {
        return visible.contains(key);
    }

    public int getPosition(String key) {
        return mDataList.indexOf(key);
    }
    public void itemReset() {
        visible = new ArrayList<>();
        visible.addAll(mDataList);
    }

    public void addList(List<String> list) {
        mDataList = new ArrayList<>();
        mDataList.addAll(list);
        itemReset();
    }

    // ViewHolder内でwidgetを割り当てる
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView text;
        TextView text2;

        public ViewHolder(View v) {
            super(v);
            imageView = (ImageView) v.findViewById(R.id.img);
            text = (TextView) v.findViewById(R.id.coupon_title_label);
            text2 = (TextView) v.findViewById(R.id.company_name_label);
        }

    }
}