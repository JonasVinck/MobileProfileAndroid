package com.commeto.kuleuven.MP.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.commeto.kuleuven.MP.interfaces.SyncInterface;

/**
 * Created by Jonas on 16/04/2018.
 */

public class SyncServiceConnection implements ServiceConnection {

    private SyncService syncService;
    private SyncInterface syncInterface;
    private boolean bound;

    public SyncServiceConnection(){
        syncService = null;
        bound = false;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        if(!bound){
            SyncService.SyncServiceBinder binder =
                    (SyncService.SyncServiceBinder) iBinder;
            syncService = binder.getService();
            syncService.setSyncInterface(syncInterface);
            bound = true;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

        syncService = null;
        bound = false;
    }

    public void setSyncInterface(SyncInterface syncInterface){
        this.syncInterface = syncInterface;
    }
}
