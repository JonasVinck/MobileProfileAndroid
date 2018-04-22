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

    /**
     * Method used to check if there exists a backup file.
     *
     * @param context Application context.
     * @return Boolean representing the existence of tje file.
     */

    public static synchronized boolean backupExists(Context context){
        try{
            return new File(context.getCacheDir(), "info").exists()
                    && new File(context.getCacheDir(), "backup").exists();
        } catch (Exception e){
            writeToLog(context, e);
            return false;
        }
    }

    /**
     * Method used to read a cache file.
     *
     * @param context Application context.
     * @param file File name.
     * @return To read String.
     */

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

    /**
     * Method used to write to internal storage.
     *
     * @param context Application context.
     * @param file File name.
     * @param content Content to be written to file.
     * @param overWrite Boolean to define if the old contents should be overwritten.
     */
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

    /**
     * Method used to write to the cache.
     *
     * @param context Application context.
     * @param file Cache file name.
     * @param content Content to be written.
     */
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

    /**
     * Method used to append to a cached file.
     *
     * @param context Application context.
     * @param file File name.
     * @param content Content to be written.
     */

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

    /**
     * Method to delete a chache file.
     *
     * @param context Application context.
     * @param file Cache file to delete.
     */
    public static synchronized void deleteFromCache(Context context, String file){
        try{
            File cache = new File(context.getCacheDir(), file);
            cache.delete();
        } catch (Exception e){
            InternalIO.writeToLog(context, e);
        }
    }

    /**
     * Method to get an InputStream from an internal file.
     *
     * @param context Application context.
     * @param stream file name.
     * @return InputStream from the given file.
     * @throws IOException Possibly thrown Exception.
     */
    public static synchronized InputStream getInputStream(Context context, String stream) throws IOException{

        try {
            return context.openFileInput(stream);
        } catch (IOException e){
            throw new IOException(e);
        }
    }

    /**
     * Method to write to a log file.
     *
     * @param context Application context.
     * @param exception Exception to be written to log.
     */

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

    /**
     * Method to write to log file.
     *
     * @param context Application context.
     * @param message Message to be written.
     */
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
}
