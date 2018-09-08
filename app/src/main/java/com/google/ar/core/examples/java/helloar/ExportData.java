package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by ISeshagiribabu on 9/8/2018.
 */

public class ExportData {

    String subFolder = "HelloARStore";

    public void writeMatrixData(Context context, float[][] adjMat, String filepath) {
        File cacheDir = null;
        File appDirectory = null;

        Log.e("FILEPATH: mat","Inside write");
        if (android.os.Environment.getExternalStorageState().
                equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = context.getExternalCacheDir();
            appDirectory = new File(cacheDir + subFolder);

        } else {
            cacheDir = context.getCacheDir();
            String BaseFolder = cacheDir.getAbsolutePath();
            appDirectory = new File(BaseFolder + subFolder);

        }

        if (appDirectory != null && !appDirectory.exists()) {
            appDirectory.mkdirs();
        }

        File fileName = new File(appDirectory, filepath);
        StringBuilder builder = new StringBuilder();

            for (int i = 0; i < adjMat.length; i++)//for each row
            {
                for (int j = 0; j < adjMat.length; j++)//for each column
                {
                    builder.append(adjMat[i][j] + "");//append to the output string
                    if (j < adjMat.length - 1)//if this is not the last row element
                        builder.append(",");//then add comma (if you don't like commas you can use spaces)
                }
                builder.append("\n");//append new line at the end of the row
            }
            Log.e("BUILDER",builder.toString());
            FileOutputStream fos = null;
            ObjectOutputStream out = null;
            try {
                fos = new FileOutputStream(fileName);
                out = new ObjectOutputStream(fos);
                out.writeUTF(builder.toString());
               // out.writeChars(builder.toString());
                //out.writeObject(builder.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }  catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null)
                        fos.flush();
                    fos.close();
                    if (out != null)
                        out.flush();
                    out.close();
                } catch (Exception e) {

                }
            }
    }


    public void writeData(Context context, HashMap hashMap, String filepath) {
        File cacheDir = null;
        File appDirectory = null;

        //Log.e("FILEPATH:","Inside write");
        //String filepath = "Hashmaps";
        if (android.os.Environment.getExternalStorageState().
                equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = context.getExternalCacheDir();
            appDirectory = new File(cacheDir + subFolder);

        } else {
            cacheDir = context.getCacheDir();
            String BaseFolder = cacheDir.getAbsolutePath();
            appDirectory = new File(BaseFolder + subFolder);

        }

        if (appDirectory != null && !appDirectory.exists()) {
            appDirectory.mkdirs();
        }

        File fileName = new File(appDirectory, filepath);
        Log.e("FILEPATH:1",appDirectory.getAbsolutePath());
        Log.e("FILEPATH:2",filepath);
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(fileName);
            out = new ObjectOutputStream(fos);
            out.writeObject(hashMap);
        } catch (IOException ex) {
            ex.printStackTrace();
        }  catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.flush();
                fos.close();
                if (out != null)
                    out.flush();
                out.close();
            } catch (Exception e) {

            }
        }
    }


    public HashMap readData(Context context, String filepath) {
        File cacheDir = null;
        File appDirectory = null;
        HashMap<Integer,Integer> myHashMap = new HashMap<>();
        if (android.os.Environment.getExternalStorageState().
                equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = context.getExternalCacheDir();
            appDirectory = new File(cacheDir + subFolder);
        } else {
            cacheDir = context.getCacheDir();
            String BaseFolder = cacheDir.getAbsolutePath();
            appDirectory = new File(BaseFolder + subFolder);
        }

        if (appDirectory != null && !appDirectory.exists()) return null; // File does not exist

        File fileName = new File(appDirectory, filepath);

        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            fis = new FileInputStream(fileName);
            in = new ObjectInputStream(fis);
            //if (filepath.equals("graphHelperHM"))
             return  (HashMap<Integer, Integer>) in.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

            try {
                if(fis != null) {
                    fis.close();
                }
                if(in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    return null;
    }

    public float[][] readMatrixData(Context context, String filepath, int size) {
        File cacheDir = null;
        File appDirectory = null;
        HashMap<Integer,Integer> myHashMap = new HashMap<>();
        if (android.os.Environment.getExternalStorageState().
                equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = context.getExternalCacheDir();
            appDirectory = new File(cacheDir + subFolder);
        } else {
            cacheDir = context.getCacheDir();
            String BaseFolder = cacheDir.getAbsolutePath();
            appDirectory = new File(BaseFolder + subFolder);
        }

        if (appDirectory != null && !appDirectory.exists()) return null; // File does not exist

        File fileName = new File(appDirectory, filepath);
        float[][] board = new float[size][size];

        try {
            int i, j;

            String line;

            FileInputStream fstream  = new FileInputStream(fileName);
            Scanner scan = new Scanner(fstream);
            DataInputStream In = new DataInputStream(fstream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(In));
int row=0;
            //Stores data into an array
            while ((line = reader.readLine()) != null)
            {
                String[] cols = line.split(","); //note that if you have used space as separator you have to split on " "
                int col = 0;
                for(String  c : cols)
                {
                    Log.e("Matrix","stri"+c);
                    board[row][col] = Integer.parseInt(c);
                    col++;
                }
                row++;
                //board[0][0] = line;
            }
            Log.e("MATRIX","board"+board);
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return board;
        }
    }
}
