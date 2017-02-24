package com.brioal.servicetest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Github : https://github.com/Brioal
 * Email : brioal@foxmial.com
 * Created by Brioal on 2017/2/24.
 */

public class ServiceBroadCaseReceiver extends BroadcastReceiver {
    private OnMsgGetListener mOnMsgGetListener;

    public ServiceBroadCaseReceiver(OnMsgGetListener onMsgGetListener) {
        mOnMsgGetListener = onMsgGetListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mOnMsgGetListener == null) {
            return;
        }
        String back = intent.getStringExtra("BackMsg");
        mOnMsgGetListener.received(back);
    }
}
