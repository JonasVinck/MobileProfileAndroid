package com.commeto.kuleuven.commetov2.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import com.commeto.kuleuven.commetov2.Activities.BaseActivity;
import com.commeto.kuleuven.commetov2.Interfaces.BaseDialogInterface;
import com.commeto.kuleuven.commetov2.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Jonas on 18/04/2018.
 */

public class BaseConfirmDialogBuilder extends AlertDialog.Builder{

    public BaseConfirmDialogBuilder(Activity activity, String title, final BaseDialogInterface dialogInterface){
        super(activity);

        this.setTitle(title);

        this.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogInterface.confirm("ok");
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
