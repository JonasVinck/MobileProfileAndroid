package com.commeto.kuleuven.MP.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.commeto.kuleuven.MP.interfaces.BaseDialogInterface;

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
