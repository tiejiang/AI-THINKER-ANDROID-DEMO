package com.bear.aithinker.a20camera;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bear.aithinker.a20camera.system.IntentUtils;
import com.bear.aithinker.a20camera.tool.DialogManager;
import com.bear.aithinker.a20camera.util.NetworkUtils;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Administrator on 2016/7/28 0028.
 */

public class ConnectActivity extends Activity implements View.OnClickListener {

    private TextView tvMsg;

    private Button btnConnect;
    private EditText ip_input;

    private ApplicationUtil appUtil;

    private Socket socket;

    private Dialog noNetworkDialog;

    private enum handler_key {
        CONNECT_OK,//连接服务器成功

        CONNECT_ERROR,//连接服务器失败

        DISCONNECT,//断开连接

        CONNECT_WIFI,//连接Wifi

        CONNECT_SERVER,//连接服务器
    }

    /**
     * The handler.
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ConnectActivity.handler_key key = ConnectActivity.handler_key.values()[msg.what];
            switch (key) {
                case CONNECT_OK:
                    Toast.makeText(ConnectActivity.this, "CONNECT_OK", Toast.LENGTH_SHORT).show();
                    IntentUtils.getInstance().startActivity(ConnectActivity.this,CameraActivity.class);
                    break;
                case CONNECT_ERROR:
                    Toast.makeText(ConnectActivity.this, "CONNECT_ERROR", Toast.LENGTH_SHORT).show();
                    break;

                case CONNECT_WIFI:
                    DialogManager.showDialog(ConnectActivity.this, noNetworkDialog);
                    break;

                case CONNECT_SERVER:
                    new BtnSocketThread().start();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        initView();
        initEvent();
        initData();
    }

    private void initData() {
        tvMsg.setVisibility(View.INVISIBLE);
        appUtil = (ApplicationUtil) ConnectActivity.this.getApplication();
        noNetworkDialog = DialogManager.getNoNetworkDialog(this);
    }

    private void initEvent() {
        btnConnect.setOnClickListener(this);
    }

    private void initView() {
        ip_input = (EditText)findViewById(R.id.ip_input);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        tvMsg = (TextView) findViewById(R.id.tvMsg);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                tvMsg.setVisibility(View.INVISIBLE);
                if (NetworkUtils.isWifiConnected(ConnectActivity.this)
//                        && NetworkUtils.getCurentWifiSSID(ConnectActivity.this).contains("Ai-Thinker-A20")) {
                        && NetworkUtils.getCurentWifiSSID(ConnectActivity.this).contains("AI-THINKER_80D89F")
                        ) {
//                    && NetworkUtils.getCurentWifiSSID(ConnectActivity.this).contains("121121121121121121121121121")) {
                    handler.sendEmptyMessage(ConnectActivity.handler_key.CONNECT_SERVER.ordinal());
                } else {
                    handler.sendEmptyMessage(ConnectActivity.handler_key.CONNECT_WIFI.ordinal());
                }
                break;
        }
    }


    /***
     * 与服务器进行socket对接
     */
    class BtnSocketThread extends Thread {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            try {
                String ip = ip_input.getText().toString();
                Log.d("TIEJIANG", "IP= " + ip);
                appUtil.initSocket(ip);
                socket = appUtil.getSocket();

            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            if (socket == null) {
                handler.sendEmptyMessage(ConnectActivity.handler_key.CONNECT_ERROR.ordinal());
            } else {
                handler.sendEmptyMessage(ConnectActivity.handler_key.CONNECT_OK.ordinal());
            }

        }
    }
}
