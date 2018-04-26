package com.commeto.kuleuven.MP.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.commeto.kuleuven.MP.interfaces.EditDialogInterface;
import com.commeto.kuleuven.MP.listeners.UnderlineButtonListener;
import com.commeto.kuleuven.MP.R;

/**
 * <pre>
 * Created by Jonas on 12/04/2018.
 *
 * Dialog used to view the description of a ride.
 * </pre>
 */

public class DescriptionDialog extends DialogFragment{

    private String description;
    private View.OnClickListener closeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismiss();
        }
    };

    /**
     * Method used to set the description.
     *
     * @param description description to be displayed.
     */
    public void set(String description){
        this.description = description;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_description, null);

        ((TextView) view.findViewById(R.id.description)).setText(description);
        view.findViewById(R.id.edit).setOnTouchListener(new UnderlineButtonListener(getActivity()));
        view.findViewById(R.id.edit).setOnClickListener(closeListener);

        builder.setView(view);
        return builder.create();
    }
}
