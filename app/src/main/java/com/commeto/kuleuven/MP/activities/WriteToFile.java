package com.commeto.kuleuven.MP.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.commeto.kuleuven.MP.R;
import com.commeto.kuleuven.MP.support.InternalIO;

import java.io.FileOutputStream;

/**
 * Created by Jonas on 19/02/2018.
 */

public class WriteToFile extends AppCompatActivity{

    private Uri uri;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_write_to_file);

        uri = null;
        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFile(getIntent().getStringExtra("format"), "filename");
            }
        });

        findViewById(R.id.open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDocument();
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uri == null){
                    Toast.makeText(getApplicationContext(), "no file selected", Toast.LENGTH_LONG).show();
                } else {
                    alterDocument(uri);
                    finish();
                }
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @TargetApi(19)
    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, 43);
    }

    @TargetApi(19)
    private void editDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("text/plain");

        startActivityForResult(intent, 44); //44 = edit request code
    }

    private void alterDocument(Uri uri) {

        try {
            ParcelFileDescriptor pfd = getApplicationContext().getContentResolver().
                    openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(getIntent().getStringExtra("data").getBytes());

            fileOutputStream.close();
            pfd.close();
        } catch (Exception e) {
            InternalIO.writeToLog(getApplicationContext(), e);
        }
    }

    @Override
    public void onActivityResult(int requestcode, int resultcode, Intent resultData){

        if((requestcode == 44 || requestcode == 43 ) && resultcode == Activity.RESULT_OK){
            if(resultData != null){
                uri = resultData.getData();
            }
        }
    }
}
