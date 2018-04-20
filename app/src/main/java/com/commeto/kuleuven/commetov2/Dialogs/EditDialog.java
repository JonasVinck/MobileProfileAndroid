package com.commeto.kuleuven.commetov2.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.commeto.kuleuven.commetov2.Interfaces.EditDialogInterface;
import com.commeto.kuleuven.commetov2.Listeners.UnderlineButtonListener;
import com.commeto.kuleuven.commetov2.R;

/**
 * Created by Jonas on 17/04/2018.
 */

public class EditDialog extends DialogFragment{

    private EditDialogInterface editDialogInterface;
    private String rideName;
    private String description;
    private View view;

    private View.OnClickListener closeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            editDialogInterface.changeDescription(
                    ((EditText) EditDialog.this.view.findViewById(R.id.ride_name)).getText().toString(),
                    ((EditText) EditDialog.this.view.findViewById(R.id.description)).getText().toString()
            );
            dismiss();
        }
    };

    public void set(String rideName, String description, EditDialogInterface editDialogInterface){
        this.rideName = rideName;
        this.description = description;
        this.editDialogInterface = editDialogInterface;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit, null);

        ((EditText) view.findViewById(R.id.description)).setText(description);
        ((EditText) view.findViewById(R.id.ride_name)).setText(rideName);
        view.findViewById(R.id.edit).setOnTouchListener(new UnderlineButtonListener(getActivity()));
        view.findViewById(R.id.edit).setOnClickListener(closeListener);

        this.view = view;
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface){
        dialogInterface.dismiss();
    }
}