package com.commeto.kuleuven.MP.listeners;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.commeto.kuleuven.MP.R;

/**
 * Created by Jonas on 15/04/2018.
 */

public class RoundedListener  implements View.OnTouchListener{

    protected Context context;

    public RoundedListener(Context context){
        this.context = context;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            view.setBackgroundResource(R.drawable.rounded_lighter);
            ((LinearLayout) view.getParent()).setBackgroundResource(R.drawable.rounded_bottom_clicked);
        }
        if(motionEvent.getAction() == MotionEvent.ACTION_UP){
            view.setBackgroundResource(R.drawable.rounded_bottom);
            ((LinearLayout) view.getParent()).setBackgroundResource(R.drawable.rounded_bottom);
        }
        return false;
    }
}
