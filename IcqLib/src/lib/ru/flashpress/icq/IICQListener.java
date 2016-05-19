package ru.flashpress.icq;

/**
 * Created by sam on 18.05.16.
 */
public interface IICQListener
{
    void icqConnected(IICQRequest request);
    void icqStarted(IICQRequest request);
    void icqReceivedMessage(ICQReceivedMessage message);
    void icqReceivedEvent(ICQReceivedEvent event);
}
