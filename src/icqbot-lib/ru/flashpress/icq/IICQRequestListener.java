package ru.flashpress.icq;

/**
 * Created by sam on 17.05.16.
 */
@FunctionalInterface
public interface IICQRequestListener
{
    void requestCompleted(IICQRequest request);
}
