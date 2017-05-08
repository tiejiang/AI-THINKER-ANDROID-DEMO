package com.bear.aithinker.a20camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bear.aithinker.a20camera.util.FileService;
import com.bear.aithinker.a20camera.view.ZoomImageView;

import java.util.List;
import java.util.Map;

/**
 * @package��com.example.imageshowerdemo
 * @author��Allen
 * @email��jaylong1302@163.com
 * @data��2012-9-27 ����10:58:13
 * @description��The class is for...
 */
public class ImageShower extends Activity {


    private Context mContext = null;
    private ViewPager mViewPager = null;
    private static String path = Environment.getExternalStorageDirectory().toString();
    private String fString;
    private List<Map<String, Object>> lsmap;
    private Bitmap[] img;
    private ImageView[] mImageViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageshower);

        mContext = ImageShower.this;
        mViewPager = (ViewPager) findViewById(R.id.id_viewPager);

        fString = path + "/AiThinker/Picture";
        //从根目录开始遍历图形文件

        img = FileService.findPic2Bitmap(fString);
        Log.i("bear", "img.length==" + img.length);
        mImageViews = new ImageView[img.length];

        mViewPager.setAdapter(new PagerAdapter() {

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ZoomImageView zoomImageView = new ZoomImageView(mContext);
                zoomImageView.setImageBitmap(img[position]);
                container.addView(zoomImageView);
                //这个ImageView数组的作用是暂存一个个的ZoomImageView对象，目的是方便以后删除
                mImageViews[position] = zoomImageView;
                return zoomImageView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                //到这里ImageView数组就派上大用场了
                container.removeView(mImageViews[position]);
            }


            @Override
            public int getCount() {
                return mImageViews.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });


    }


}
