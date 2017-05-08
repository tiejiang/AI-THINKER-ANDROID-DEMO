package com.bear.aithinker.a20camera.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2016/7/9 0009.
 */

public class FileService {
    private static String sdState = Environment.getExternalStorageState();
    private static String path = Environment.getExternalStorageDirectory().toString();


    public static boolean saveToSDCard(byte buf[], String imageName) {
        File file;
        File PicName;
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            //获得sd卡根目录
            file = new File(path + "/AiThinker/Picture");
            if (!file.exists()) {
                file.mkdirs();
            }
            PicName = new File(file, imageName);
            try {
                if (!PicName.exists()) {
                    PicName.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(PicName, true);
                fos.write(buf);
                fos.flush();
                fos.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public static void saveBitmap(Bitmap bitmap, String imageName) {
        File file;
        File PicName;
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            //获得sd卡根目录
            file = new File(path + "/AiThinker/Picture");
            if (!file.exists()) {
                file.mkdirs();
            }
            PicName = new File(file, imageName);
            try {
                if (!PicName.exists()) {
                    PicName.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(PicName);
                if (PicName.getName().endsWith(".png")) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                } else if (PicName.getName().endsWith(".jpg")) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                }
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //从SD卡取
    public static Bitmap getBitmap(String imageName) {
        Bitmap bitmap = null;
        File imagePic;
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {

            imagePic = new File(path + "/AiThinker/Picture", imageName);
            if (imagePic.exists()) {
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(imagePic));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    //将SD卡文件删除

    public static void deleteFile(String imageName) {
        File file = new File(path + "/AiThinker/Picture", imageName);
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            if (file.exists()) {
                if (file.isFile()) {
                    file.delete();
                }

                file.delete();
            }
        }
    }


    /**
     * 图片反转
     *
     * @param img
     * @return
     */
    public static Bitmap toturn(Bitmap img) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90); /*翻转90度*/
        int width = img.getWidth();
        int height = img.getHeight();
        img = Bitmap.createBitmap(img, 0, 0, width, height, matrix, true);
        return img;
    }

    /*****
     * 通过SD路径获得所有图片，将其转换成bitmap
     * @param fpath
     * @return Bitmap[]
     */
    public static Bitmap[] findPic2Bitmap(String fpath) {

        File fl = new File(fpath);
        File[] fls = fl.listFiles();
        Bitmap[] b = new Bitmap[fls.length];

        if (fls != null) {
            for (int i = 0; i < fls.length; i++) {
                if (fls[i].isDirectory()) {
                    findPic2Bitmap(fls[i].getAbsolutePath());
                } else {
                    String fname= fls[i].getName();
                    String fpathString = fls[i].getAbsolutePath();
                    String ext = fpathString.substring(fpathString.lastIndexOf("."), fpathString.length());
                    if (ext.equalsIgnoreCase(".jpg") || ext.equalsIgnoreCase(".png") || ext.equalsIgnoreCase(".icon")) {
                        b[i] = FileService.getBitmap(fname);
                    }
                }
            }
        }
        return b;
    }


}
