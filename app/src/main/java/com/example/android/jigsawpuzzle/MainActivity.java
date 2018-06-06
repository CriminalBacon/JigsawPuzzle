package com.example.android.jigsawpuzzle;


import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

 @Override
    protected void onCreate(Bundle savedInstanceState){
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_main);

     AssetManager assetManager = getAssets();
     try{
         final String[] files = assetManager.list("img");
         GridView gridView = findViewById(R.id.grid);
         gridView.setAdapter(new ImageAdapter(this));

         gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Intent intent = new Intent(getApplicationContext(), PuzzleActivity.class);
                 intent.putExtra("assetName", files[position % files.length]);
                 startActivity(intent);
             } //onItemClick
         });

     } catch (IOException e){
         Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
     } //catch
 }


} //MainActivity
