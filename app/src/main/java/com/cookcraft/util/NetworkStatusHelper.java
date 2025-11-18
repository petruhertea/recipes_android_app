package com.cookcraft.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class NetworkStatusHelper extends LiveData<Boolean> {
    private Context context;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    public NetworkStatusHelper(Context context) {
        this.context = context.getApplicationContext();
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onActive() {
        super.onActive();
        updateNetworkStatus();

        connectivityManager.registerDefaultNetworkCallback(getNetworkCallback());
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    private ConnectivityManager.NetworkCallback getNetworkCallback() {
        if (networkCallback == null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    postValue(true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    postValue(false);
                }
            };
        }
        return networkCallback;
    }

    private void updateNetworkStatus() {
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            postValue(false);
            return;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        postValue(capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)));
    }
}