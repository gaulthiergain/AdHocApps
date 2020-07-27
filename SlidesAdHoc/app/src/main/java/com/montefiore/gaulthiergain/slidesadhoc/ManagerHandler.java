package com.montefiore.gaulthiergain.slidesadhoc;

import android.content.Context;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.TransferManager;

public class ManagerHandler {

    private static TransferManager transferManager;

    public static synchronized TransferManager getTransferManager(Context context) {
        transferManager.updateContext(context);
        return transferManager;
    }

    public static synchronized void setTransferManager(TransferManager transferManager) {
        ManagerHandler.transferManager = transferManager;
    }
}
