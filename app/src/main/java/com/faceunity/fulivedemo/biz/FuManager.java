package com.faceunity.fulivedemo.biz;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.faceunity.fulivedemo.view.EffectAndFilterSelectAdapter;
import com.faceunity.fulivedemo.view.TestActivity;
import com.faceunity.wrapper.faceunity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by arno on 2017/9/6.
 */

public class FuManager {
    private FuModule mFuModule;
    private String mEffectFileName;
    private CreateItemHandler mCreateItemHandler;

    public FuManager(Context context) {
        HandlerThread ht = new HandlerThread("CreateItemThread");
        ht.start();
        mCreateItemHandler = new CreateItemHandler(ht.getLooper(), context);
        mEffectFileName = EffectAndFilterSelectAdapter.EFFECT_ITEM_FILE_NAME[1];
        mFuModule = new FuModule();
    }

    public FuModule getModule() {
        return mFuModule;
    }

    public String getEffectFileName() {
        return mEffectFileName;
    }

    public void setEffectItem(String effectItemName) {
        if (mFuModule.mEffectItem != 0 && effectItemName.equals(mEffectFileName)) {
            return;
        }
        mEffectFileName = effectItemName;
        removeMessage();
        mCreateItemHandler.sendEmptyMessage(FuManager.CreateItemHandler.HANDLE_CREATE_ITEM);
    }

    public void removeMessage() {
        mCreateItemHandler.removeMessages(CreateItemHandler.HANDLE_CREATE_ITEM);
    }

    public void quite() {
        mCreateItemHandler.getLooper().quit();
    }

    private class CreateItemHandler extends Handler {

        static final int HANDLE_CREATE_ITEM = 1;

        WeakReference<Context> mContext;

        CreateItemHandler(Looper looper, Context context) {
            super(looper);
            mContext = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            Context context = mContext.get();
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLE_CREATE_ITEM:
                    try {
                        if (mEffectFileName.equals("none")) {
                            mFuModule.mEffectItem = 0;
                            //itemsArray[1] = mEffectItem = 0;
                        } else {
                            InputStream is = context.getAssets().open(mEffectFileName);
                            byte[] itemData = new byte[is.available()];
                            int len = is.read(itemData);
                            Log.i("FU", "effect len " + len);
                            is.close();
                            //final int tmp = itemsArray[1];
                            //itemsArray[1] = mEffectItem = faceunity.fuCreateItemFromPackage(itemData);
                            final int tmp = mFuModule.mEffectItem;
                            mFuModule.mEffectItem = faceunity.fuCreateItemFromPackage(itemData);
                            faceunity.fuItemSetParam(mFuModule.mEffectItem, "isAndroid", 1.0);
                            faceunity.fuItemSetParam(mFuModule.mEffectItem, "rotationAngle",
                                    ((TestActivity) mContext.get()).getCameraFacingDriection()
                                            == Camera.CameraInfo.CAMERA_FACING_FRONT ? 90 : 270);
                            if (tmp != 0) {
                                faceunity.fuDestroyItem(tmp);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }



}
