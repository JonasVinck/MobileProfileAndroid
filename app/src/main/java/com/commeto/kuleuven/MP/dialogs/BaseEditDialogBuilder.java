package com.commeto.kuleuven.MP.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.commeto.kuleuven.MP.interfaces.BaseDialogInterface;
import com.commeto.kuleuven.MP.R;

/**
 * Created by Jonas on 18/04/2018.
 */

public class BaseEditDialogBuilder extends AlertDialog.Builder{

    public BaseEditDialogBuilder(Activity activity, String title, String previous, final BaseDialogInterface dialogInterface){
        super(activity);

        this.setTitle(title);

        final EditText input = new EditText(activity);
        input.setTextColor(activity.getResources().getColor(R.color.black));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(15, 15, 15,15);
        input.setLayoutParams(params);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(previous);
        this.setView(input);

        this.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogInterface.confirm(input.getText().toString());
                if(dialog != null) dialog.dismiss();
            }
        });
        this.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null) dialog.dismiss();
            }
        });

    }
}
