package com.faceunity.fulivedemo.biz;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import com.faceunity.fulivedemo.common.Consts;
import com.faceunity.fulivedemo.common.authpack;
import com.faceunity.wrapper.faceunity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by arno on 2017/9/12.
 */

public class FuDrawer {
    private static final String TAG = "FuDrawer";
    //Tip:camera texture类型是默认的是OES的，和texture 2D不同
    private static final boolean IS_OESTEXTURE = true;
    //是否初始化美化
    private final boolean IS_USE_BEAUTY = true;
    //是否初始化手势
    private final boolean IS_USE_GESTURE = false;

    FuManager mFuControler ;
    FuModule mFu;

    private byte[] mCacheCameraNV21Bytes;
    //是否需要写回，如果是，则入参的byte[]会被修改为带有fu特效的；支持写回自定义大小的内存数组中，即readback custom img
    private boolean mIsNeedReadBack = false;
    //摄像机方向
    private int mCameraFacingDirection = Camera.CameraInfo.CAMERA_FACING_FRONT;
    //帧记录
    private int mFrameId = 0;


    public FuDrawer(Context context) {
        mFuControler = new FuManager(context);
        mFu = mFuControler.getModule();
    }

    public FuDrawer(FuManager fuControler) {
        mFuControler = fuControler;
        mFu = mFuControler.getModule();
    }

    public void setCameraFacingDirection(int cameraFacingDirection) {
        mCameraFacingDirection = cameraFacingDirection;
    }

    /**
     * 初始化fu设置的一些参数
     */
    public void init() {
        try {
            //权限
            InputStream is = Consts.appContext.getAssets().open("v3.mp3");
            byte[] v3data = new byte[is.available()];
            int len = is.read(v3data);
            is.close();
            faceunity.fuSetup(v3data, null, authpack.A());
            faceunity.fuSetMaxFaces(2);//设置最大识别人脸数目
            Log.e(TAG, "fuSetup v3 len " + len);
            //美化
            if (IS_USE_BEAUTY) {
                is = Consts.appContext.getAssets().open("face_beautification.mp3");
                byte[] itemData = new byte[is.available()];
                len = is.read(itemData);
                Log.e(TAG, "beautification len " + len);
                is.close();
                mFu.mFaceBeautyItem = faceunity.fuCreateItemFromPackage(itemData);
            }
            //手势
            if (IS_USE_GESTURE) {
                is = Consts.appContext.getAssets().open("heart.mp3");
                byte[] itemData = new byte[is.available()];
                len = is.read(itemData);
                Log.e(TAG, "heart len " + len);
                is.close();
                mFu.mGestureItem = faceunity.fuCreateItemFromPackage(itemData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mFuControler.setEffectItem(mFuControler.getEffectFileName());
    }

    public void clearFrameId() {
        mFrameId = 0;
    }

    /**
     *
     * @param cameraNV21Byte
     * @param cameraTextureId
     * @param cameraWidth
     * @param cameraHeight
     * @return 返回渲染完的textureid
     */
    public int draw(byte[] cameraNV21Byte, int cameraTextureId, int cameraWidth, int cameraHeight) {
        updateBeauty();
        int flags = updateReadBack(cameraNV21Byte);
        //这里拿到fu处理过后的texture，可以对这个texture做后续操作，如硬编、预览。
        int rt = faceunity.fuDualInputToTexture(cameraNV21Byte, cameraTextureId, flags,
                cameraWidth, cameraHeight, mFrameId++, mFu.itemArray());
        return rt;
    }



    private int updateReadBack(byte[] cameraNV21Byte) {
        int flags = IS_OESTEXTURE ? faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE : 0;
        //是否需要写回，如果是，则入参的byte[]会被修改为带有fu特效的；支持写回自定义大小的内存数组中，即readback custom img
        flags = mIsNeedReadBack ? flags | faceunity.FU_ADM_FLAG_ENABLE_READBACK : flags;
        if (mIsNeedReadBack) {
            if (mCacheCameraNV21Bytes == null) {
                mCacheCameraNV21Bytes = new byte[cameraNV21Byte.length];
            }
            System.arraycopy(cameraNV21Byte, 0, mCacheCameraNV21Bytes, 0, cameraNV21Byte.length);
        } else {
            mCacheCameraNV21Bytes = cameraNV21Byte;
        }
        flags |= mCameraFacingDirection == Camera.CameraInfo.CAMERA_FACING_FRONT ? 0 : faceunity.FU_ADM_FLAG_FLIP_X;
        return flags;
    }

    private void updateBeauty() {
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "color_level", mFu.mFaceBeautyColorLevel);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "blur_level", mFu.mFaceBeautyBlurLevel);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "filter_name", mFu.mFilterName);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "cheek_thinning", mFu.mFaceBeautyCheekThin);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "eye_enlarging", mFu.mFaceBeautyEnlargeEye);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "face_shape", mFu.mFaceShape);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "face_shape_level", mFu.mFaceShapeLevel);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "red_level", mFu.mFaceBeautyRedLevel);
    }




}
