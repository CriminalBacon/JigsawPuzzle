package com.example.android.jigsawpuzzle;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static java.lang.Math.abs;

public class PuzzleActivity extends AppCompatActivity {

    ArrayList<PuzzlePiece> pieces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        final RelativeLayout layout = findViewById(R.id.layout);
        final ImageView imageView = findViewById(R.id.image_view);

        Intent intent = getIntent();
        final String assetName = intent.getStringExtra("assetName");


        //run image related code after the view was laid out
        // to have all dimensions calculated

        imageView.post(new Runnable() {
            @Override
            public void run() {
                if (assetName != null){
                    setPicFromAsset(assetName, imageView);
                }

                pieces = splitImage();
                TouchListener touchListener = new TouchListener();

                //shuffle pieces order
                Collections.shuffle(pieces);

                for (PuzzlePiece piece : pieces){
                    piece.setOnTouchListener(touchListener);
                    layout.addView(piece);

                    //randomize position on the bottom of the screen
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) piece.getLayoutParams();
                    layoutParams.leftMargin = new Random().nextInt(layout.getWidth() - piece.pieceWidth);
                    layoutParams.topMargin = layout.getHeight() - piece.pieceHeight;
                    piece.setLayoutParams(layoutParams);


                } //for

            } //run
        });

    } //onCreate

    private ArrayList<PuzzlePiece> splitImage(){
        int piecesNumber = 12;
        int rows = 4;
        int cols = 3;

        ImageView imageView = findViewById(R.id.image_view);
        ArrayList<PuzzlePiece> pieces = new ArrayList<>(piecesNumber);

        //Get the bitmap of the source image

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        int[] dimensions = getBitMapPositionInsideImageView(imageView);
        int scaledBitmapLeft = dimensions[0];
        int scaledBitmapTop = dimensions[1];
        int scaledBitmapWidth = dimensions[2];
        int scaledBitmapHeight = dimensions[3];

        int croppedImageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft);
        int croppedImageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledBitmapWidth, scaledBitmapHeight, true);
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, abs(scaledBitmapLeft), abs(scaledBitmapTop),
                croppedImageWidth, croppedImageHeight);

        //Calculate the width and the height of the pieces
        int pieceWidth = croppedImageWidth/cols;
        int pieceHeight = croppedImageHeight/rows;

        //Create each bitmap piece and add it to the resulting array
        int yCoord = 0;

        for (int row = 0; row < rows; row++){
            int xCoord = 0;

            for (int col = 0; col < cols; col++){
                //calculate the offset for each piece
                int offsetX = 0;
                int offsetY = 0;

                if (col > 0){
                    offsetX = pieceWidth / 3;
                } //if

                if (row > 0){
                    offsetY = pieceHeight / 3;
                } //if

                //apply the offset to each piece
                Bitmap pieceBitmap = Bitmap.createBitmap(croppedBitmap, xCoord - offsetX, yCoord - offsetY,
                        pieceWidth + offsetX, pieceHeight + offsetY);
                PuzzlePiece piece = new PuzzlePiece(getApplicationContext());
                piece.setImageBitmap(pieceBitmap);
                piece.xCoord = xCoord - offsetX + imageView.getLeft();
                piece.yCoord = yCoord - offsetY + imageView.getTop();
                piece.pieceWidth = pieceWidth + offsetX;
                piece.pieceHeight = pieceHeight + offsetY;

                //this bitmap will hold our final puzzle piece image
                Bitmap puzzlePiece = Bitmap.createBitmap(pieceWidth + offsetX, pieceHeight + offsetY, Bitmap.Config.ARGB_8888);

                //draw path
                int bumpSize = pieceHeight / 4;
                Canvas canvas = new Canvas(puzzlePiece);
                Path path = new Path();
                path.moveTo(offsetX, offsetY);
                if (row == 0){
                    //top side piece
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                } else {
                    //top bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3, offsetY);
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6,
                            offsetY - bumpSize,
                            offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5,
                            offsetY - bumpSize,
                            offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2,
                            offsetY);
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                } //else

                if (col == cols - 1){
                    //right side piece
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());

                } else {
                    //right bump
                    path.lineTo(pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY)/ 3);
                    path.cubicTo(pieceBitmap.getWidth() - bumpSize,
                            offsetY + (pieceBitmap.getHeight() - offsetY) / 6,
                            pieceBitmap.getWidth() - bumpSize,
                            offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5,
                            pieceBitmap.getWidth(),
                            offsetY + (pieceBitmap.getHeight() - offsetY) /3 * 2);
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());


                } //else

                if (row == rows - 1){
                    //bottom side piece
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                } else {
                    //bottom bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2, pieceBitmap.getHeight());
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5,
                            pieceBitmap.getHeight() - bumpSize,
                            offsetX + (pieceBitmap.getWidth() - offsetX) / 6,
                            pieceBitmap.getHeight() - bumpSize,
                            offsetX + (pieceBitmap.getWidth() - offsetX) / 3,
                            pieceBitmap.getHeight());
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                }


                if (col == 0){
                    //left side piece
                    path.close();
                } else {
                    //left bump
                    path.lineTo(offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3 * 2);
                    path.cubicTo(offsetX - bumpSize,
                            offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5,
                            offsetX - bumpSize,
                            offsetY + (pieceBitmap.getHeight() - offsetY) / 6,
                            offsetX,
                            offsetY + (pieceBitmap.getHeight() - offsetY) / 3);
                    path.close();
                } //else

                //mask the piece
                Paint paint = new Paint();
                paint.setColor(0xFF000000);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawPath(path, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(pieceBitmap, 0, 0, paint);

                //draw a white border
                Paint border = new Paint();
                border.setColor(0x80FFFFFF);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(8.0f);
                canvas.drawPath(path, border);

                //draw a black border
                border = new Paint();
                border.setColor(0x80000000);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(3.0f);
                canvas.drawPath(path, border);

                //set  the resulting bitmap to the piece
                piece.setImageBitmap(puzzlePiece);
                pieces.add(piece);
                xCoord += pieceWidth;



            } //for

            yCoord += pieceHeight;

        } //for

        return pieces;

    } //splitImage

    private int[] getBitMapPositionInsideImageView(ImageView imageView){
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null){
            return ret;
        } //if

        //Get Image dimensions
        // get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        //Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        //get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        //Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        //Get image position
        //We assume that the image is centered into ImageView

        int imgViewWidth = imageView.getWidth();
        int imgViewHeight = imageView.getHeight();

        int top = (int) (imgViewHeight - actH)/2;
        int left = (int) (imgViewWidth - actW)/2;

        ret[0] = left;
        ret[1] = top;

        return ret;

    } //getBitMapPositionInsideImageView


    private void setPicFromAsset(String assetName, ImageView imageView){
        //get the dimesions of the view
        int targetWidth = imageView.getWidth();
        int targetHeight = imageView.getHeight();

        AssetManager assetManager = getAssets();
        try{
            InputStream inputStream = assetManager.open("img/" + assetName);
            //get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, new Rect(-1, -1, -1, -1), bmOptions);
            int photoWidth = bmOptions.outWidth;
            int photoHeight = bmOptions.outHeight;

            //determing how much to scale down the image
            int scaleFactor = Math.min(photoWidth/targetWidth, photoHeight/targetHeight);

            inputStream.reset();

            //deocode the image file into the Bitmap sized to fill the view
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, new Rect(-1, -1, -1, -1), bmOptions);
            imageView.setImageBitmap(bitmap);

        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        } //catch

    } //setPicFromAsset



} //PuzzleActivity
