package com.commeto.kuleuven.MP.listeners;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.commeto.kuleuven.MP.R;

/**
 * Created by Jonas on 16/03/2018.
 *
 * <p>
 * Listener used for the standard tab_inner styled LinearLayouts.
 * </p>
 */

public class UnderlineButtonListener implements View.OnTouchListener{

    protected Context context;

    public UnderlineButtonListener(Context context){
        this.context = context;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            view.setBackgroundColor(context.getResources().getColor(R.color.ontouch));
            ((LinearLayout) view.getParent()).setBackgroundColor(
                    context.getResources().getColor(R.color.accent)
            );
        }
        if(motionEvent.getAction() == MotionEvent.ACTION_UP){
            view.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
            ((LinearLayout) view.getParent()).setBackgroundColor(
                    context.getResources().getColor(R.color.colorPrimaryDark)
            );
        }
        return false;
    }
}
