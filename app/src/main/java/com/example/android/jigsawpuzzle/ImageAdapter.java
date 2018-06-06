package com.example.android.jigsawpuzzle;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mt on 6/6/18.
 */

public class ImageAdapter extends BaseAdapter{

    private Context mContext;
    private AssetManager assetManager;
    private String[] files;

    public ImageAdapter(Context context){
        mContext = context;
        assetManager = mContext.getAssets();

        try {
            files = assetManager.list("img");

        } catch(IOException e) {
            e.printStackTrace();
        } //catch


    } //ImageAdapter


    @Override
    public int getCount() {
        return files.length;
    } //getCount

    @Override
    public Object getItem(int position) {
        return null;
    } //getItem

    @Override
    public long getItemId(int position) {
        return 0;
    } //getItemId

    //create a new ImageView for each item referenced by the adapter

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.grid_element, null);
        }//if

        final ImageView imageView = convertView.findViewById(R.id.grid_image_view);
        imageView.setImageBitmap(null);

        //run image realted code after the view was laid out
        imageView.post(new Runnable() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, Void>(){
                    private Bitmap bitmap;

                    @Override
                    protected Void doInBackground(Void... params) {
                        bitmap = getPicFromAsset(imageView, files[position]);
                        return null;
                    } //doInBackground

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        imageView.setImageBitmap(bitmap);
                    }
                }.execute(); //AsyncTask
            }
        });

        return convertView;
    } //getView

    private Bitmap getPicFromAsset(ImageView imageView, String assetName){
        //get the dimensions of the View
        int targetWidth = imageView.getWidth();
        int targetHeight = imageView.getHeight();

        if (targetWidth == 0 || targetHeight == 0){
            //view has no dimensions set
            return null;
        } //if

        try{
            InputStream inputStream = assetManager.open("img/" + assetName);
            //Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, new Rect(-1, -1, -1, -1), bmOptions);
            int photoWidth = bmOptions.outWidth;
            int photoHeight = bmOptions.outHeight;

            //Determine how much to scale down the image
            int scaleFactor = Math.min(photoWidth/targetWidth, photoHeight/targetHeight);

            inputStream.reset();

            //Decode the image file into a Bitmap sized to fill the view
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            return BitmapFactory.decodeStream(inputStream, new Rect(-1, -1, -1, -1), bmOptions);

        } catch (IOException e){
            e.printStackTrace();
            return null;
        } //catch

    } //getPicFromAsset


} //class ImageAdapter
