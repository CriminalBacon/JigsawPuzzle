package com.example.android.jigsawpuzzle;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by mt on 6/5/18.
 */

public class TouchListener implements View.OnTouchListener {
    private float xDelta;
    private float yDelta;


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float x = motionEvent.getRawX();
        float y = motionEvent.getRawY();
        final double tolerance = sqrt(pow(view.getWidth(), 2) + pow(view.getHeight(), 2))/10;
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;


        PuzzlePiece piece = (PuzzlePiece) view;
        if (!piece.canMove){
            return true;
        } //if

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                xDelta = x - layoutParams.leftMargin;
                yDelta = y - layoutParams.topMargin;
                piece.bringToFront();
                break;
            case MotionEvent.ACTION_MOVE:
                layoutParams.leftMargin = (int) (x - xDelta);
                layoutParams.topMargin = (int) (y - yDelta);
                layoutParams.bottomMargin = (int) (screenHeight - (y - piece.pieceHeight));
                layoutParams.rightMargin = (int) (screenWidth - (x - piece.pieceWidth));
                view.setLayoutParams(layoutParams);
                break;
            case MotionEvent.ACTION_UP:
                int xDiff = abs(piece.xCoord - layoutParams.leftMargin);
                int yDiff = abs(piece.yCoord - layoutParams.topMargin);
                if (xDiff <= tolerance && yDiff <= tolerance){
                    layoutParams.leftMargin = piece.xCoord;
                    layoutParams.topMargin = piece.yCoord;
                    piece.setLayoutParams(layoutParams);
                    piece.canMove = false;
                    sendViewToBack(piece);

                } //if

        } //switch


        return true;
    } //onTouch

    private void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup) child.getParent();
        if (null != parent){
            parent.removeView(child);
            parent.addView(child, 0);
        } //if
    } //sendViewToBack



} //TouchListener
