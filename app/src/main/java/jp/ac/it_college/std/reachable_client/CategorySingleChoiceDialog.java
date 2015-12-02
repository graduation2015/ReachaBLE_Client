package jp.ac.it_college.std.reachable_client;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CategorySingleChoiceDialog extends ChoiceDialog {

    private int checkedItem = -1;
    private String checkedCategory = "";

    @Override
    protected Bundle makeArgs(Context context) {
        Bundle args = new Bundle();
        String[] items = context.getResources().getStringArray(R.array.categories);
        args.putStringArray(ITEMS, items);

        return args;
    }

    @Override
    protected Dialog makeDialog() {
        String[] items = getArguments().getStringArray(ITEMS);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Select Category")
                .setSingleChoiceItems(items, checkedItem, makeSingleChoiceListener(items))
                .setPositiveButton("OK", makeConfirmClickListener())
                .setNegativeButton("Cancel", makeCancelClickListener())
                .create();
    }

    private DialogInterface.OnClickListener makeSingleChoiceListener(final String[] items) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkedItem = i;
                checkedCategory = items[i];
            }
        };
    }

    /**
     * OK押下時の処理
     * @return
     */
    private DialogInterface.OnClickListener makeConfirmClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //チェックしたカテゴリのリストを親フラグメントに渡す
                Intent intent = new Intent();
                intent.putExtra(CHECKED_ITEMS, checkedCategory);
                getTargetFragment().onActivityResult(getTargetRequestCode(), i, intent);
            }
        };
    }

    /**
     * キャンセル押下時の処理
     * @return
     */
    protected DialogInterface.OnClickListener makeCancelClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "makeCancelClickListener");
            }
        };
    }
}