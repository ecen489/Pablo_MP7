package com.example.pablo_mp7;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    SQLiteDatabase db;

    int ID = 0;

    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create table
        String q = "CREATE TABLE IF NOT EXISTS imageTb ( "
                + " pic_id INTEGER PRIMARY KEY, "
                + " image BLOB NOT NULL "
                + " )";

        //Create database
        db = openOrCreateDatabase("images.db", Context.MODE_PRIVATE,null);

        db.execSQL(q);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            img = (ImageView) findViewById(R.id.pic);
            img.setImageBitmap(imageBitmap);
        }
    }

    public void grabpic(View view) {
        dispatchTakePictureIntent();
    }

    public  void storepic(View view) throws SQLiteException {
        storing();
    }

    private void storing() throws SQLiteException {

        try{
            img.buildDrawingCache();
            Bitmap bmp = img.getDrawingCache();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            byte[] imagedata = stream.toByteArray();

            String sqlstate = "INSERT INTO imageTb (pic_id, image) VALUES (?,?)";
            SQLiteStatement inserting = db.compileStatement(sqlstate);
            inserting.bindLong(1, ID);
            inserting.bindBlob(2, imagedata);

            //ContentValues cont = new ContentValues();

            //cont.put("pic_id", Integer.toString(ID));
            //cont.put("image", imagedata);

            //db.insert("imagetb",null, cont);

            inserting.executeInsert();
            Toast.makeText(this,"Done, the ID of this photo is " + Integer.toString(ID), Toast.LENGTH_SHORT).show();
            inserting.clearBindings();



            ID++;

        }catch(SQLiteException w) {
            ID++;
            storing();
        }
    }
    public void showpic(View view) {
        EditText val = (EditText) findViewById(R.id.RetrieveID);
        String userid = val.getText().toString();

        if(isNumeric(userid)) {

            Toast.makeText(this, userid, Toast.LENGTH_SHORT).show();

            Cursor c = db.rawQuery("SELECT * FROM imageTb WHERE pic_id = " + userid, null);

            if (c.moveToFirst()) {
                do {
                    byte[] image = c.getBlob(c.getColumnIndex("image"));
                    Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                    img.setImageBitmap(bmp);
                    Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
                } while (c.moveToNext());
            }
            c.close();
        } else {
            Toast.makeText(this, "Please put an Integer, don't try to be funny kid", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}
