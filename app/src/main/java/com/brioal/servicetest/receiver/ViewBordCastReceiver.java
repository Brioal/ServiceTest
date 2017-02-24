package com.brioal.servicetest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 接收来自View的消息的
 * Github : https://github.com/Brioal
 * Email : brioal@foxmial.com
 * Created by Brioal on 2017/2/24.
 */

public class ViewBordCastReceiver extends BroadcastReceiver {
    private OnMsgGetListener mGetListener;

    public ViewBordCastReceiver(OnMsgGetListener getListener) {
        mGetListener = getListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mGetListener == null) {
            return;
        }
        String content = intent.getStringExtra("ViewMsg");
        mGetListener.received(content);
    }
}
