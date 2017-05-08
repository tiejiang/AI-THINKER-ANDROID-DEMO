package com.bear.aithinker.a20camera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bear.aithinker.a20camera.titlemenu.ActionItem;
import com.bear.aithinker.a20camera.titlemenu.TitlePopup;
import com.bear.aithinker.a20camera.tool.DialogManager;
import com.bear.aithinker.a20camera.tool.MyRoundProcess;
import com.bear.aithinker.a20camera.util.FileService;
import com.bear.aithinker.a20camera.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Administrator on 2016/7/28 0028.
 */

public class CameraActivity extends Activity implements View.OnClickListener {

    private ImageView ivBack;

    private ImageView ivJpg;

    private ImageView ivPhoto;

    private TextView tvSave, tvRotation, tvRadioBtn, tvTime;


    private ApplicationUtil appUtil;

    private Socket socket;

    private PrintWriter out;

    private DataInputStream is;

    private String StrRadio, SendData, RecvData;

    private TitlePopup titlePopup;

    private ProgressDialog photoDialog = null;

    private MyRoundProcess mRoundProcess;
    private MyRoundProcess r;

    private int myState = 0;

    private Boolean isConnect = false;

    private static final String FILENAME = "myjpg.jpg";

    private Bitmap img = null;

    private Thread TimerThread;


    private enum handler_key {
        CONNECT_OK,//连接服务器成功
        DISCONNECT,//断开连接
        RECECT_BEGIN,//收到bigin数据
        RECECT_DATA_ERROR,//接收数据错误
        RECECT_DATA_OK,//接收数据正确
        RECECT_JPG,//收到图片
        TiME_COUNT,//时间超时
    }

    private static int STATE_DISCONNECT = 0;
    private static int STATE_BEGIN = 1;
    private static int STATE_RECEVICE = 2;

    private final int DATASIZE = 4100;
    private final int RESULTSIZE = 4096;
    private byte buf[] = new byte[DATASIZE];
    private byte getBuf[] = new byte[DATASIZE];
    private byte ResultBuf[];

    private int packagelen = 0;//长度
    private int packageNum = 0;//商
    private int packageRem = 0;//余

    private int len = 0;//计算收到包的长度
    private int sum = 0;//progress
    private int count = 0;//数据接收包的次数
    private int currentPackage = 0;//当前读到的包数
    private int mylength = 0; //计算是否达到一个包的要求
    private int myTime = 0; //计算时间
    private int ERRORNUM = 3;//错误次数


    /**
     * The handler.
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CameraActivity.handler_key key = CameraActivity.handler_key.values()[msg.what];
            switch (key) {
                case CONNECT_OK:
                    break;
                case DISCONNECT:
                    Toast.makeText(CameraActivity.this, "CONNECT_ERROR", Toast.LENGTH_SHORT).show();
                    try {
                        socket.close();
                        is.close();
                        out.close();
                        isConnect = false;
                        cleanRecvData(1);
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case RECECT_BEGIN:
                    photoDialog.cancel();
                    DealJson();
                    if (packagelen == 0) {
                        Toast.makeText(CameraActivity.this, "PHOTO_FALSE", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CameraActivity.this, "PHOTO_OK", Toast.LENGTH_SHORT).show();
                        StartRead();//开始读取数据
                    }
                    break;
                case RECECT_DATA_ERROR:
                    Toast.makeText(CameraActivity.this, "RECECT_DATA_ERROR", Toast.LENGTH_SHORT).show();
                    ERRORNUM--;
                    if (ERRORNUM == 0) {
                        ERRORNUM = 3;
                    } else {
                        SendData = "at+camrd=" + count * 4096 + "," + (count * 4096 + 4095);
                        SendtoServer(SendData);
                    }
                    cleanRecvData(0);
                    break;
                case RECECT_DATA_OK:
//                    Log.i("bear","====ok=====");
                    myTime = 0;
                    SendData = "ok";
                    SendtoServer(SendData);
                    mRoundProcess.setProgress((sum * 100) / ((packageNum + 1) * 4100));
                    break;
                case RECECT_JPG:
                    myTime = 0;
                    mRoundProcess.setVisibility(View.INVISIBLE);
                    BtnEnable(true);
                    img = FileService.getBitmap(FILENAME);
                    ivJpg.setImageBitmap(img);
                    Toast.makeText(CameraActivity.this, "RECECT_JPG", Toast.LENGTH_SHORT).show();
                    break;
                case TiME_COUNT:
                    tvTime.setText(myTime + "");
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initView();
        initEvent();
        initData();
        initsocket();
        new BtnTestRecvThread().start();
        TimerThread = new Thread(new ThreadShow());
        TimerThread.start();
    }

    private void initsocket() {
        appUtil = (ApplicationUtil) CameraActivity.this.getApplication();
        socket = appUtil.getSocket();
        out = appUtil.getOut();
        is = appUtil.getIn();
        isConnect = true;
    }

    private void initData() {
        StrRadio = "begin2";

        mRoundProcess.setVisibility(View.INVISIBLE);
        img = FileService.getBitmap(FILENAME);
        ivJpg.setImageBitmap(img);
    }

    private void initEvent() {
        ivJpg.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivPhoto.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        tvRotation.setOnClickListener(this);
        tvRadioBtn.setOnClickListener(this);
    }

    private void initView() {
        ivBack = (ImageView) findViewById(R.id.ivBack);
        ivJpg = (ImageView) findViewById(R.id.ivJpg);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        tvSave = (TextView) findViewById(R.id.tvSave);
        tvRotation = (TextView) findViewById(R.id.tvRotation);
        tvRadioBtn = (TextView) findViewById(R.id.tvRadioBtn);
        tvTime = (TextView) findViewById(R.id.tvTime);

        r = new MyRoundProcess(CameraActivity.this);
        mRoundProcess = (MyRoundProcess) findViewById(R.id.my_round_process);
        photoDialog = DialogManager.progressDialog(CameraActivity.this, "正在拍照...");
        initPopWindow();

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivBack:
                cleanRecvData(1);
                finish();
                break;
            case R.id.ivPhoto:
                myTime = 0;
                photoDialog.show();
                SendData = StrRadio;
                SendtoServer(StrRadio);
                SetState(STATE_BEGIN);
                cleanRecvData(1);
                break;
            case R.id.tvRadioBtn:
                titlePopup.show(this.findViewById(R.id.tvRadioBtn));
                break;
            case R.id.tvRotation:
                if (img != null) {
                    img = FileService.toturn(img);
                    ivJpg.setImageBitmap(img);
                }
                break;
            case R.id.tvSave:
                if (img != null) {
                    String img_name = StringUtils.getCharacterAndNumber() + ".jpg";
                    Log.i("bear", "保存成功：" + img_name);
                    FileService.saveBitmap(img, img_name);
                    Toast.makeText(CameraActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CameraActivity.this, "请先读取图片", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.ivJpg:
                startActivity(new Intent(CameraActivity.this, ImageShower.class));
                break;
        }
    }


    /***
     * 发送数据到服务器
     */
    private void SendtoServer(String string) {
        StringBuffer sb = StringUtils.StringAddLine(string);
        // 是否连接到服务器
        if ((!socket.isClosed()) && (socket.isConnected())) {
            if (!socket.isOutputShutdown()) {
                Log.i("bear", "[SendtoServer]sb=" + sb);
                out.print(sb);
                out.flush();
            }
        }
    }

    private void StartRead() {
        mRoundProcess.setVisibility(View.VISIBLE);
        mRoundProcess.setProgress(0);
        BtnEnable(false);

        SetState(STATE_RECEVICE);
        SendData = "read";
        SendtoServer(SendData);
        FileService.deleteFile(FILENAME);
    }


    private void SetState(int intState) {
        myState = intState;
    }

    /***
     * 实例化标题栏弹窗
     */
    private void initPopWindow() {

        titlePopup = new TitlePopup(this, ViewPager.LayoutParams.WRAP_CONTENT,
                ViewPager.LayoutParams.WRAP_CONTENT);
        titlePopup.setItemOnClickListener(onitemClick);
        // 给标题栏弹窗添加子类
        titlePopup.addAction(new ActionItem(this, R.string.VGA,
                R.drawable.move_icon));
        titlePopup.addAction(new ActionItem(this, R.string.QVGA,
                R.drawable.move_icon));
        titlePopup.addAction(new ActionItem(this, R.string.QQVGA,
                R.drawable.move_icon));
    }

    private TitlePopup.OnItemOnClickListener onitemClick = new TitlePopup.OnItemOnClickListener() {
        @Override
        public void onItemClick(ActionItem item, int position) {
            switch (position) {
                case 0:// BEGIN1
                    StrRadio = "begin1";
                    break;
                case 1:// BEGIN0
                    StrRadio = "begin0";
                    break;
                case 2:// BEGIN2
                    StrRadio = "begin2";
                    break;
                default:
                    break;
            }
        }
    };

    /***
     * 解析JSON数据，获取相应的数据包长度，包的数量和余数
     */
    private void DealJson() {
        try {
            JSONObject json = new JSONObject(RecvData);
            Log.i("bear", "json" + json);
            packagelen = Integer.parseInt(json.getString("size"));
            packageNum = packagelen / 4096;
            packageRem = packagelen % 4096;
            Log.i("bear", "packagelen:" + packagelen + ",packageNum=" + packageNum + "packageRem" + packageRem);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /***
     * 解析协议包  前两位是 EE 00(包数) + DATASIZE + FF FF
     *
     * @param b
     * @return true or false
     */
    private Boolean DealPackageIsRight(byte[] b) {
        if (b == null || b.length <= 0) {
            return false;
        }
        String str0 = StringUtils.byteToHexString(b[0]);
        String str1 = StringUtils.byteToHexString(b[1]);//number
        String str2 = StringUtils.byteToHexString(b[DATASIZE - 2]);
        String str3 = StringUtils.byteToHexString(b[DATASIZE - 1]);
        Log.i("bear", ">>>>[getBuf]:" + str0 + str1 + str2 + str3);
        currentPackage = Integer.parseInt(str1, 16);
        if ((str0.equals("ee")) && (currentPackage == count) && (str2.equals("cc")) && (str3.equals("ff"))) {
            handler.sendEmptyMessage(handler_key.RECECT_DATA_OK.ordinal());
            return true;
        } else {
            handler.sendEmptyMessage(handler_key.RECECT_DATA_ERROR.ordinal());
            return false;
        }
    }


    private void DealBufSize() {
        if (currentPackage == packageNum) {
            ResultBuf = new byte[packageRem];
            System.arraycopy(getBuf, 2, ResultBuf, 0, packageRem);//截取中间数据
            Log.i("bear", "[packageRem]=" + packageRem + "[ResultBuf]=" + StringUtils.bytesToHexString(ResultBuf));
            Log.i("bear", ">>>[" + currentPackage + "]是否保存：" + FileService.saveToSDCard(ResultBuf, FILENAME));
            handler.sendEmptyMessage(handler_key.RECECT_JPG.ordinal());
        } else {
            ResultBuf = new byte[RESULTSIZE];
            System.arraycopy(getBuf, 2, ResultBuf, 0, RESULTSIZE);//截取中间数据
            Log.i("bear", "[RESULTSIZE]=" + RESULTSIZE + "[ResultBuf]=" + StringUtils.bytesToHexString(ResultBuf));
            Log.i("bear", ">>>[" + currentPackage + "]是否保存：" + FileService.saveToSDCard(ResultBuf, FILENAME));
        }
    }

    public void cleanRecvData(int num) {
        mylength = 0;
        ResultBuf = null;
        getBuf = null;
        getBuf = new byte[DATASIZE];
        if (num == 1) {
            count = 0; //计算清0
            currentPackage = 0;//计算清0
            sum = 0; //dialog 清0
        }
    }

    /****
     * 设置读取数据过程的dialog
     *
     * @param b
     */
    private void BtnEnable(boolean b) {
        ivJpg.setEnabled(b);
        ivPhoto.setEnabled(b);
        tvSave.setEnabled(b);
        tvRotation.setEnabled(b);
    }


    /***
     * 接收数据的thread
     */
    class BtnTestRecvThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (isConnect) {
                if ((!socket.isClosed()) && (socket.isConnected())) {
                    // 是否接收到数据
                    if (!socket.isInputShutdown()) {
                        try {
                            //读取接收的数据
                            if (is.available() < DATASIZE) {
                                buf = null;
                                buf = new byte[DATASIZE];
                            }
                            len = is.read(buf);
                            Log.i("bear", "len=" + len);
//                            Log.i("bear", StringUtils.bytesToHexString(buf));
                            if (len < 0) {
                                SetState(STATE_DISCONNECT);
                            }
                            switch (myState) {
                                case 0:
                                    is.close();
                                    handler.sendEmptyMessage(handler_key.DISCONNECT.ordinal());
                                    break;
                                case 1:
                                    RecvData = new String(buf, 0, buf.length);
                                    handler.sendEmptyMessage(handler_key.RECECT_BEGIN.ordinal());
                                    break;
                                case 2:
                                    sum += len;
                                    if (mylength != DATASIZE) {
                                        System.arraycopy(buf, 0, getBuf, mylength, len);//截取中间数据
                                        mylength += len;
                                        Log.i("bear", "[mylength]=" + mylength + "[getBuf]=" + StringUtils.bytesToHexString(getBuf));
                                    }
                                    if (mylength == DATASIZE) {
                                        if (DealPackageIsRight(getBuf)) {
                                            DealBufSize();
                                            count++;
                                            cleanRecvData(0);
                                        }
                                    }
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }

    // 线程类
    class ThreadShow implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                try {
                    Thread.sleep(1000);
                    myTime++;
                    if (myTime > 15) {
                        myTime = 0;
                        photoDialog.cancel();
                        mRoundProcess.setVisibility(View.INVISIBLE);
                        BtnEnable(true);
                        r.setProgress((float) 6.6 * myTime);
                    }
                    handler.sendEmptyMessage(handler_key.TiME_COUNT.ordinal());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.out.println("thread error...");
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        img = FileService.getBitmap(FILENAME);
        ivJpg.setImageBitmap(img);
    }

}
