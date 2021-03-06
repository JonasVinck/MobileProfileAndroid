package com.commeto.kuleuven.MP.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.commeto.kuleuven.MP.interfaces.EditDialogInterface;
import com.commeto.kuleuven.MP.listeners.UnderlineButtonListener;
import com.commeto.kuleuven.MP.R;

/**
 * <pre>
 * Created by Jonas on 17/04/2018.
 *
 * Dialog used to edit the name and description of a ride. Action to be taken on confirm specified
 * in interface.
 * </pre>
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

    /**
     * Method used to set the information to be displayed.
     *
     * @param rideName            Name of the ride to be displayed.
     * @param description         Description of the ride. to be displayed.
     * @param editDialogInterface Interface used to return the information.
     */
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
}