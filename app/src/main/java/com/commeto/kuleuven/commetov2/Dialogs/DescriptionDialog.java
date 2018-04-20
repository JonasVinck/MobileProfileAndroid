package com.commeto.kuleuven.commetov2.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.commeto.kuleuven.commetov2.Interfaces.EditDialogInterface;
import com.commeto.kuleuven.commetov2.Listeners.UnderlineButtonListener;
import com.commeto.kuleuven.commetov2.R;

/**
 * Created by Jonas on 12/04/2018.
 */

public class DescriptionDialog extends DialogFragment{

    private EditDialogInterface editDialogInterface;
    private String description;

    private View view;
    private View.OnClickListener closeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismiss();
        }
    };

    public void set(String description){
        this.description = description;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_description, null);

        ((TextView) view.findViewById(R.id.description)).setText(description);
        view.findViewById(R.id.edit).setOnTouchListener(new UnderlineButtonListener(getActivity()));
        view.findViewById(R.id.edit).setOnClickListener(closeListener);

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface){
        dialogInterface.dismiss();
    }
}
