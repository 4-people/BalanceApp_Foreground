package com.example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.GoogleMapAPI.GoogleMapActivity;

public class MyAlarm extends BroadcastReceiver {
    //    public void onReceive(Context context, Intent intent){
//        Log.d("옥","알람 반복");
//
//
//
//    }
    public static final String TAG = "LOCATION STATE";
    private static String mLastState;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "MyAlarm.java안의 onReceive()"); /** * http://mmarvick.github.io/blog/blog/lollipop-multiple-broadcastreceiver-call-state/ * 2번 호출되는 문제 해결 */
        String state = intent.getStringExtra(LocationManager.GPS_PROVIDER);

        intent = new Intent(context, GoogleMapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

//        if (state.equals(mLastState)) {
//            return;
//        } else {
//            mLastState = state;
//        }
//        if (LocationManager.KEY_LOCATION_CHANGED.equals(state)) {
//            String incomingNumber = intent.getStringExtra(LocationManager.GPS_PROVIDER);
//            final String phone_number = PhoneNumberUtils.formatNumber(incomingNumber);
//            Intent serviceIntent = new Intent(context, ShowMsgActivity.class);
//            serviceIntent.putExtra(ShowMsgActivityer);
//            context.startService(serviceIntent);
//        }
    }


}
