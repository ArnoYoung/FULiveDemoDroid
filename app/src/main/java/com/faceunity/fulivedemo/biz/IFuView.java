package com.faceunity.fulivedemo.biz;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.faceunity.fulivedemo.util.encoder.TextureMovieEncoder;

/**
 * Created by arno on 2017/9/13.
 */

public interface IFuView extends TextureMovieEncoder.OnEncoderStatusUpdateListener{
    Context getContext();
    GLSurfaceView getGlsv();
    void onFrameTracking(boolean isTracking);

}