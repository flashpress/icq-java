package ru.flashpress.icq;

/**
 * Created by sam on 18.05.16.
 */
public interface IICQListener
{
    void icqReceivedMessage(ICQReceivedMessage message);
    default void icqConnected(IICQRequest request) {ICQDebug.out("icqConnected:", request);}
    default void icqStarted(IICQRequest request) {ICQDebug.out("icqStarted:", request);}
    default void icqReceivedEvent(ICQReceivedEvent event) {ICQDebug.out("icqReceivedEvent:", event.eventData);}
}
