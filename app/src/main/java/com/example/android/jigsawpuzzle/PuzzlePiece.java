package com.example.android.jigsawpuzzle;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;

/**
 * Created by mt on 6/5/18.
 */

public class PuzzlePiece extends AppCompatImageView {

    public int xCoord;
    public int yCoord;
    public int pieceWidth;
    public int pieceHeight;
    public boolean canMove = true;

    public PuzzlePiece(Context context) {
        super(context);
    } //PuzzlePiece


} // class PuzzlePiece
