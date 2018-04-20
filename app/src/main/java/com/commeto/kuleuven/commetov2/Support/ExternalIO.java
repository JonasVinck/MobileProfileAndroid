package com.commeto.kuleuven.commetov2.Support;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileOutputStream;

import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static com.commeto.kuleuven.commetov2.Support.Static.makeToastLong;
import static com.commeto.kuleuven.commetov2.Support.Static.tryLogin;

/**
 * Created by Jonas on 7/04/2018.
 */

public class ExternalIO {

    @TargetApi(19)
    public static void createFile(Activity activity, String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(activity, intent, 43, null); //43 = write request code
    }

    public static void alterDocument(Context context, String toWrite, int requestcode, int resultcode, Intent resultData) {

        if((requestcode == 44 || requestcode == 43) && resultcode == Activity.RESULT_OK){
            if(resultData != null && resultData.getData() != null){

                try {
                    ParcelFileDescriptor pfd = context.getContentResolver().
                            openFileDescriptor(resultData.getData(), "w");
                    FileOutputStream fileOutputStream =
                            new FileOutputStream(pfd.getFileDescriptor());
                    fileOutputStream.write(toWrite.getBytes());
                    fileOutputStream.close();
                    pfd.close();
                } catch (Exception e) {
                    InternalIO.writeToLog(context, e);
                    makeToastLong(context, "er ging iets fout bij het opslaan");
                }
            }
        }
    }
}
