package com.commeto.kuleuven.MP.listeners;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Jonas on 13/04/2018.
 */

public class MenuIconUnderlineListener extends UnderlineButtonListener{

    private ImageView imageView;
    private int nonClick, click;
    private boolean enable;

    public MenuIconUnderlineListener(Context context, ImageView imageView, int nonClick, int click){
        super(context);
        this.imageView = imageView;
        this.nonClick = nonClick;
        this.click = click;
        enable = true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent){

        if(enable) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setImageDrawable(context.getResources().getDrawable(click));
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                imageView.setImageDrawable(context.getResources().getDrawable(nonClick));
            }
        }

        return super.onTouch(view, motionEvent);
    }

    public void setEnable(boolean enable){
        this.enable = enable;
    }
}
