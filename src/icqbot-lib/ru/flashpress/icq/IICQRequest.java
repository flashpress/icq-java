package ru.flashpress.icq;

import org.json.simple.JSONObject;

/**
 * Created by sam on 18.05.16.
 */
public interface IICQRequest
{
    int getStatusCode();
    String getStatusText();
    JSONObject getData();
    boolean isRunning();
    boolean isFailed();

    void addListener(IICQRequestListener listener);
    void cancel();
    void release();
}
