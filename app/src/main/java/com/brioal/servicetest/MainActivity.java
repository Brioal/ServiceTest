package com.brioal.servicetest;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.brioal.servicetest.receiver.OnMsgGetListener;
import com.brioal.servicetest.receiver.ServiceBroadCaseReceiver;
import com.brioal.servicetest.service.MsgService;

public class MainActivity extends AppCompatActivity implements OnMsgGetListener {
    private Button mBtnStart;
    private EditText mEt;
    private Button mBtnSend;
    private TextView mTvMsg;
    private EditText mEtHost;
    private EditText mEtPort;
    private Button mBtnQiandao;
    private ServiceBroadCaseReceiver mCaseReceiver;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEtHost = (EditText) findViewById(R.id.main_et_host);
        mBtnQiandao = (Button) findViewById(R.id.main_btn_close);
        mEtPort = (EditText) findViewById(R.id.main_et_port);
        mBtnStart = (Button) findViewById(R.id.main_btn_start);
        mEt = (EditText) findViewById(R.id.main_et_send);
        mBtnSend = (Button) findViewById(R.id.main_btn_setup);
        mTvMsg = (TextView) findViewById(R.id.main_tv_msg);
        bindServiceBackReceiver();
        //服务开启和关闭
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    mBtnStart.setText("关闭");
                    closeMsg();
                    isRunning = false;
                } else {
                    mBtnStart.setText("开启");
                    startMsg();
                    isRunning = true;
                }
            }
        });
        //发送
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEt.getText().toString() + System.currentTimeMillis();
                sendMsgService(content);
            }
        });
        //签到
        mBtnQiandao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    //绑定接收来自service消息的Receiver
    private void bindServiceBackReceiver() {
        mCaseReceiver = new ServiceBroadCaseReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.brioal.servicetest.receiver.ServiceBroadCaseReceiver");
        MainActivity.this.registerReceiver(mCaseReceiver, filter);
    }


    @Override
    public void received(String msg) {
        mTvMsg.setText(msg);
    }

    //发送消息到Service
    private void sendMsgService(String msg) {
        Intent intent = new Intent();
        intent.putExtra("ViewMsg", msg);
        intent.setAction("com.brioal.servicetest.receiver.ViewBordCastReceiver");
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(MainActivity.this, MsgService.class));
        unregisterReceiver(mCaseReceiver);
        super.onDestroy();
    }

    //启动消息
    private void startMsg() {
        String host = mEtHost.getText().toString();
        int port = Integer.parseInt(mEtPort.getText().toString());
        Intent intent = new Intent(MainActivity.this, MsgService.class);
        intent.putExtra("Host", host);
        intent.putExtra("Port", port);
        startService(intent);
    }

    //关闭消息
    private void closeMsg() {
        stopService(new Intent(MainActivity.this, MsgService.class));
    }
}
