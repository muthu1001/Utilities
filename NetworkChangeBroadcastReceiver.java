package com.thanthi.dtnext.dtnextapplication.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.thanthi.dtnext.dtnextapplication.utils.Utils;

public class NetworkChangeBroadcastReceiver extends BroadcastReceiver {

    private NetworkListener networkListener;

    public NetworkChangeBroadcastReceiver(NetworkListener networkListener) {
        this.networkListener = networkListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        networkListener.networkStatus(Utils.isNetworkAvailable(context));

    }
}
