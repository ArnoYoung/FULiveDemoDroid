package com.faceunity.fulivedemo;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.faceunity.wrapper.faceunity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by arno on 2017/9/6.
 */

public class FuControler implements IFuView {
    private FuModule mFu = new FuModule();
    private String mEffectFileName = EffectAndFilterSelectAdapter.EFFECT_ITEM_FILE_NAME[1];
    CreateItemHandler mCreateItemHandler;

    public FuControler(Context context) {
        HandlerThread ht = new HandlerThread("CreateItemThread");
        ht.start();
        mCreateItemHandler = new CreateItemHandler(ht.getLooper(), context);
    }

    public FuModule getFu() {
        return mFu;
    }

    public String getEffectFileName() {
        return mEffectFileName;
    }

    @Override
    public void onBlurLevelSelected(int level) {
        switch (level) {
            case 0:
                mFu.mFaceBeautyBlurLevel = 0;
                break;
            case 1:
                mFu.mFaceBeautyBlurLevel = 1.0f;
                break;
            case 2:
                mFu.mFaceBeautyBlurLevel = 2.0f;
                break;
            case 3:
                mFu.mFaceBeautyBlurLevel = 3.0f;
                break;
            case 4:
                mFu.mFaceBeautyBlurLevel = 4.0f;
                break;
            case 5:
                mFu.mFaceBeautyBlurLevel = 5.0f;
                break;
            case 6:
                mFu.mFaceBeautyBlurLevel = 6.0f;
                break;
        }
    }

    @Override
    public void onCheekThinSelected(int progress, int max) {
        mFu.mFaceBeautyCheekThin = 1.0f * progress / max;
    }

    @Override
    public void onColorLevelSelected(int progress, int max) {
        mFu.mFaceBeautyColorLevel = 1.0f * progress / max;
    }

    @Override
    public void onEffectItemSelected(String effectItemName) {
        if ( mFu.mEffectItem != 0 && effectItemName.equals(mEffectFileName)) {
            return;
        }
        mEffectFileName = effectItemName;
        removeMessage();
        mCreateItemHandler.sendEmptyMessage(CreateItemHandler.HANDLE_CREATE_ITEM);
    }

    @Override
    public void onEnlargeEyeSelected(int progress, int max) {
        mFu.mFaceBeautyEnlargeEye = 1.0f * progress / max;
    }

    @Override
    public void onFilterSelected(String filterName) {
        mFu.mFilterName = filterName;
    }

    @Override
    public void onRedLevelSelected(int progress, int max) {
        mFu.mFaceBeautyRedLevel = 1.0f * progress / max;
    }


    @Override
    public void onFaceShapeLevelSelected(int progress, int max) {
        mFu.mFaceShapeLevel = (1.0f * progress) / max;
    }

    @Override
    public void onFaceShapeSelected(int faceShape) {
        mFu.mFaceShape = faceShape;
    }

    public void removeMessage(){
        mCreateItemHandler.removeMessages(CreateItemHandler.HANDLE_CREATE_ITEM);
    }

    public void quite(){
        mCreateItemHandler.getLooper().quit();
    }

    class CreateItemHandler extends Handler {

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
                            mFu.mEffectItem = 0;
                            //itemsArray[1] = mEffectItem = 0;
                        } else {
                            InputStream is = context.getAssets().open(mEffectFileName);
                            byte[] itemData = new byte[is.available()];
                            int len = is.read(itemData);
                            Log.i("FU", "effect len " + len);
                            is.close();
                            //final int tmp = itemsArray[1];
                            //itemsArray[1] = mEffectItem = faceunity.fuCreateItemFromPackage(itemData);
                            final int tmp = mFu.mEffectItem;
                            mFu.mEffectItem = faceunity.fuCreateItemFromPackage(itemData);
                            faceunity.fuItemSetParam(mFu.mEffectItem, "isAndroid", 1.0);
                            faceunity.fuItemSetParam(mFu.mEffectItem, "rotationAngle",
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
