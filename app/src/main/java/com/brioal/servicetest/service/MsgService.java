package com.brioal.servicetest.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.brioal.servicetest.receiver.OnMsgGetListener;
import com.brioal.servicetest.receiver.ViewBordCastReceiver;
import com.socks.library.KLog;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Github : https://github.com/Brioal
 * Email : brioal@foxmial.com
 * Created by Brioal on 2017/2/24.
 */

public class MsgService extends Service implements OnMsgGetListener {
    private Socket mSocket;
    private DataOutputStream mDataOut;//输出流
    private DataInputStream mDataIn;//输入流
    private String mHost = "192.168.191.1";//主机地址
    private int mPort = 60000;//端口号
    private boolean isRunning = false;
    private List<String> mMsg = new ArrayList<>();
    private ViewBordCastReceiver mCastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHost = intent.getStringExtra("Host");
        mPort = intent.getIntExtra("Port", 60000);
        KLog.e("Host:" + mHost);
        KLog.e("Port:" + mPort);
        bindViewService();
        isRunning = true;
        initFreeSocket();//初始化心跳
        initConnectedSocket();//初始化Socket链接
        initSendThread();//初始化发送
        initReceived();//初始化接收
        return super.onStartCommand(intent, flags, startId);
    }

    private void bindViewService() {
        mCastReceiver = new ViewBordCastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.brioal.servicetest.receiver.ViewBordCastReceiver");
        MsgService.this.registerReceiver(mCastReceiver, filter);
    }

    //创建发送心跳的保温
    private void initFreeSocket() {
        new Thread() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        addMsg("Free Msg");
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }
        }.start();
    }

    private void addMsg(String msg) {
        mMsg.add(msg);
    }

    //初始化Socket
    private void initSocket() {
        try {
            while (mDataOut == null || mDataIn == null) {
                mSocket = new Socket(mHost, mPort);
                mDataOut = new DataOutputStream(mSocket.getOutputStream());
                mDataIn = new DataInputStream(mSocket.getInputStream());
            }
        } catch (Exception e) {//服务器未开启
            e.printStackTrace();
        }
    }

    //创建持续的连接
    private void initConnectedSocket() {
        Thread mInitThread = new Thread(new Runnable() {
            public void run() {
                while (isRunning) {
                    try {
                        if (!isConnected()) {
                            //没有连接
                            initSocket();//初始化链接
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mInitThread.start();
    }

    private boolean isConnected() {
        if (mSocket != null && mDataIn != null && mDataOut != null && mSocket.isConnected()) {
            return true;
        }
        return false;
    }

    //创建持续的接收链接
    private void initReceived() {
        Thread mReceivedThread = new Thread(new Runnable() {
            public void run() {
                while (isRunning) {
                    try {
                        if (mDataIn != null) {
                            int r = mDataIn.available();
                            while (r == 0 && mSocket.isConnected()) {
                                if (mDataIn != null) r = mDataIn.available();
                            }
                            byte[] b = new byte[r];
                            mDataIn.read(b);
                            StringBuffer buffer = new StringBuffer();
                            for (int i = 0; i < b.length; i++) {
                                buffer.append(b[i]);
                            }

                            final String content = new String(b, "utf-8");
                            KLog.e(buffer.toString());
                            sendMsgView(buffer.toString());
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mReceivedThread.start();
    }


    //创建一直发送的线程
    public void initSendThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (isRunning) {
                    synchronized (ArrayList.class) {
                        if (mMsg.size() > 0) {
                            try {
                                sendMsg(mMsg.get(0));
                                mMsg.remove(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    //发送设置报文
    private void sendMsg(final String msg) {
        try {
            if (mDataOut != null) {
                OutputStreamWriter outSW = new OutputStreamWriter(mDataOut, "GBK");
                BufferedWriter bw = new BufferedWriter(outSW);
                KLog.e("SendMessage" + msg);
                bw.write(msg);
                bw.flush();
                outSW.flush();
                mDataOut.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void received(String msg) {
        mMsg.clear();
        addMsg("3000000300010d0030303030303030303030303031010b00313338393336353639323002000101003001040032313639ad59".trim());
    }

    //发送消息到View
    private void sendMsgView(String msg) {
        //发送广播
        Intent intent = new Intent();
        intent.putExtra("BackMsg", msg);
        intent.setAction("com.brioal.servicetest.receiver.ServiceBroadCaseReceiver");
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        unregisterReceiver(mCastReceiver);
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
