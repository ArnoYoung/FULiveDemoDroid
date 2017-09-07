package com.faceunity.fulivedemo.gles.utils;

import java.util.Arrays;

/**
 * Created by arno on 2017/9/7.
 */

public class ArrayObject {

    private float[] mDatas;
    private float[][] mCache;

    /**
     *
     * @param datas 数据
     * @param cacheSize 缓存大小
     */
    public ArrayObject(float[] datas,int cacheSize) {
        mDatas = datas;
        mCache = new float[cacheSize][];
    }


    public float[] cache(int index){
        return mCache[index];
    }

    /**
     * 更新指定缓存的长度，数据丢失
     * @param index
     * @param lenght
     * @return
     */
    public float[] updateCache(int index,int lenght){
        if (mCache[index] == null || mCache[index].length != lenght){
            mCache[index] = new float[lenght];
        }
        return mCache[index];
    }

    /**
     * 拷贝目标到指定的缓存
     * @param index
     * @param cache
     * @return
     */
    public float[] updateCache(int index,float[] cache){
        if (mCache[index] == null || mCache[index].length != cache.length){
            mCache[index] = Arrays.copyOf(cache,cache.length);
        }else {
            System.arraycopy(cache,0,mCache[index],0,cache.length);
        }
        return mCache[index];
    }

    /**
     * @deprecated  这种设置方式不安全，推荐update
     * @param datas
     */
    public void setDatas(float[] datas) {
        mDatas = datas;
    }

    /**
     * 数据长度
     * @return
     */
    public int length(){
        return mDatas.length;
    }

    /**
     * 返回元数据
     * @return
     */
    public float[] datas() {
        return mDatas;
    }

    /**
     * 更新数据的长度和内容
     * @param datas
     * @return
     */
    public float[] updateData(float[] datas){
        if (mDatas == null || datas.length != mDatas.length){
            mDatas = Arrays.copyOf(datas,datas.length);
        }else {
            System.arraycopy(datas,0,mDatas,0,datas.length);
        }
        return mDatas;
    }

    /**
     * 更新数据长度,数据将会丢失
     * @param length
     * @return
     */
    public float[] updateData(int length){
        if (mDatas == null || length != mDatas.length){
            mDatas = new float[length];
        }
        return mDatas;
    }

}
