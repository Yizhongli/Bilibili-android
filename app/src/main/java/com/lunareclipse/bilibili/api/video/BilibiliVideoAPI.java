package com.lunareclipse.bilibili.api.video;

import com.alibaba.fastjson.JSON;
import com.lunareclipse.bilibili.api.support.BilibiliResponse;
import com.lunareclipse.bilibili.api.support.URLConstants;
import com.lunareclipse.bilibili.api.support.BilibiliCallback;
import com.lunareclipse.bilibili.model.AVInfo;
import com.lunareclipse.bilibili.model.MPlayURL;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by LunarEclipse on 16/8/24.
 */
public class BilibiliVideoAPI
{
    enum VideoQuality
    {
        LOW,
        NORMAL,
        HIGH           //Not supported.
    }

    private static final OkHttpClient client = new OkHttpClient();

    /**
     *
     * @param aid Av number of video.
     * @param quality Video quality.
     * @param page The page number, start from 1.
     * @param bilibiliCallback  Handle success, failure & progress of the request.
     */
    public static void getVideoURL(int aid, int page, VideoQuality quality, BilibiliCallback<String> bilibiliCallback)
    {
        switch (quality)
        {
            case LOW: getLowQualityVideoURL(aid, page, bilibiliCallback); break;
            case NORMAL: getNormalQualityVideoURL(aid, page, bilibiliCallback); break;
            default: break;
        }
    }

    /**
     *
     * @param aid
     * @param bilibiliCallback
     */
    public static void getAVInfo(int aid, final BilibiliCallback<AVInfo> bilibiliCallback)
    {
        Request request = new Request.Builder().url(URLConstants.BILIBILIJJ_AV2CID + aid).build();

        client.newCall(request).enqueue(new okhttp3.Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                if (bilibiliCallback != null)
                    bilibiliCallback.onFailure(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                String content = response.body().string();
                AVInfo info = JSON.parseObject(content, AVInfo.class);
                if (bilibiliCallback != null)
                    bilibiliCallback.onSuccess(info, new BilibiliResponse(0, "ok"));
            }
        });
    }

    private static void getLowQualityVideoURL(int aid, final int page, final BilibiliCallback<String> bilibiliCallback)
    {
        Request request = new Request.Builder().url(URLConstants.BILIBILI_VIDEO_PLAYURL_M).build();

        client.newCall(request).enqueue(new okhttp3.Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                if (bilibiliCallback != null)
                    bilibiliCallback.onFailure(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                String content = response.body().string();
                MPlayURL mplayURL = JSON.parseObject(content, MPlayURL.class);
                String realPlayURL = mplayURL.getSrc();

                BilibiliResponse biliResponse = new BilibiliResponse(0, "ok");
                biliResponse.request = response.request();
                biliResponse.response = response;

                if (bilibiliCallback != null)
                    bilibiliCallback.onSuccess(realPlayURL, biliResponse);
            }
        });
    }

    private static void getNormalQualityVideoURL(int aid, final int page, final BilibiliCallback<String> bilibiliCallback)
    {
        getAVInfo(aid, new BilibiliCallback<AVInfo>()
        {
            @Override
            public void onSuccess(AVInfo object, BilibiliResponse biliResponse)
            {
                //Get origin url.
                String jjurl = object.getList().get(page - 1).getMp4Url();

                //Redirect to real play-url.
                Request request = new Request.Builder().url(jjurl).method("HEAD", null).build();
                client.newCall(request).enqueue(new okhttp3.Callback()
                {
                    @Override
                    public void onFailure(Call call, IOException e)
                    {
                        if (bilibiliCallback != null)
                            bilibiliCallback.onFailure(null);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException
                    {
                        String url =  response.request().url().toString();
                        if (bilibiliCallback != null)
                            bilibiliCallback.onSuccess(url, new BilibiliResponse(0, "ok"));
                    }
                });
            }

            @Override
            public void onFailure(BilibiliResponse biliResponse)
            {
                if (bilibiliCallback != null)
                    bilibiliCallback.onFailure(null);
            }
        });
    }
}
