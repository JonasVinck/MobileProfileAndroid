package com.commeto.kuleuven.MP.support;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;

import java.io.FileOutputStream;

import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static com.commeto.kuleuven.MP.support.Static.makeToastLong;

/**
 * Created by Jonas on 7/04/2018.
 *
 * Static methi-ods to write to external memory.
 */

public class ExternalIO {

    /**
     * Method used to create a file.
     *
     * @param activity Calling activity.
     * @param mimeType Type of the file.
     * @param fileName File name.
     */

    @TargetApi(19)
    public static void createFile(Activity activity, String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(activity, intent, 43, null); //43 = write request code
    }

    /**
     * Method used to write data to an external file.
     *
     * @param context Application context.
     * @param toWrite String that has to be written to the file.
     * @param requestcode Request code from OnActivityResult.
     * @param resultcode Result code from OnActivityResult.
     * @param resultData Result data from OnActivityResult.
     */

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
