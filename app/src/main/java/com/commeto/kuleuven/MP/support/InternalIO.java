package com.commeto.kuleuven.MP.support;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static com.commeto.kuleuven.MP.http.HTTPStatic.convertInputStreamToString;
import static com.commeto.kuleuven.MP.support.Static.makeToastLong;

/**
 * Created by Jonas on 1/03/2018.
 */

public class InternalIO {
//==================================================================================================
    //public functions

    public static synchronized boolean backupExists(Context context){
        try{
            return new File(context.getCacheDir(), "info").exists()
                    && new File(context.getCacheDir(), "backup").exists();
        } catch (Exception e){
            writeToLog(context, e);
            return false;
        }
    }

    public static synchronized String readFromInternal(Context context, String file) {

        try {

            FileInputStream fileInputStream = context.openFileInput(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                sb.append(line);
            }

            bufferedReader.close();
            return sb.toString();
        } catch (IOException e) {
            makeToastLong(context, e.getMessage());
        }

        return null;
    }

    public static synchronized String readFromCache(Context context, String file){

        try {

            File open = new File(context.getCacheDir(), file);
            FileInputStream fileInputStream = new FileInputStream(open);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                sb.append(line);
            }

            bufferedReader.close();
            return sb.toString();
        } catch (IOException e) {
            makeToastLong(context, e.getMessage());
        }

        return null;
    }

    public static synchronized void writeToInternal(Context context, String file, String content, boolean overWrite) {

        try {
            FileOutputStream outputStream;
            File internalFile = new File(file);
            if(!internalFile.exists() || overWrite) {
                outputStream = context.openFileOutput(file, Context.MODE_PRIVATE);
                outputStream.write(content.getBytes());
                outputStream.close();
            }
        } catch (IOException e) {
            makeToastLong(context, e.getMessage());
        }
    }

    public static synchronized void writeToCache(Context context, String file, String content){

        try {
            FileOutputStream outputStream;

            File open = new File(context.getCacheDir(), file);
            open.createNewFile();
            outputStream = new FileOutputStream(open);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            makeToastLong(context, e.getMessage());
        }
    }

    public static synchronized void appendToCache(Context context, String file, String content){

        try {
            FileOutputStream outputStream;

            File open = new File(context.getCacheDir(), file);
            open.createNewFile();
            outputStream = new FileOutputStream(open);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.append(content);
            writer.close();
        } catch (IOException e) {
            makeToastLong(context, e.getMessage());
        }
    }

    public static synchronized void deleteFromCache(Context context, String file){
        try{
            File cache = new File(context.getCacheDir(), file);
            cache.delete();
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }

    public static synchronized InputStream getInputStream(Context context, String stream) throws IOException{

        try {
            return context.openFileInput(stream);
        } catch (IOException e){
            throw new IOException(e);
        }
    }

    public static synchronized void writeToLog(Context context, Exception exception){

        try{
            File log = new File(context.getFilesDir(), "log");
            if((log.createNewFile())){
                Long length = log.length();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(log));
            writer.append(exception.toString());
            writer.append("======================================================================");
            writer.close();
        } catch (Exception e){
        }
    }

    public static synchronized void writeToLog(Context context, String message){

        try{
            File log = new File(context.getFilesDir(), "log");
            if((log.createNewFile())){
                Long length = log.length();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(log));
            writer.append(message);
            writer.append("======================================================================");
            writer.close();
        } catch (Exception e){
        }
    }

    public static synchronized String getLog(Context context){

        try {
            return convertInputStreamToString(getInputStream(
                    context,
                    "log"
            ));
        } catch (Exception e){
            return "Log is leeg.";
        }
    }

    public static boolean deleteLog(Context context){
        File log = new File(context.getFilesDir(), "log");
        if(!log.exists()) return true;
        return log.delete();
    }
}
