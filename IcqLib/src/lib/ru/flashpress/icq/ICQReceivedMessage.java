package ru.flashpress.icq;

/**
 * Created by sam on 18.05.16.
 */
public class ICQReceivedMessage extends ICQReceivedEvent
{
    ICQReceivedMessage()
    {

    }

    long time;
    public long getTime() {return time;}

    String msgId;
    public String getMsgId() {return msgId;}

    String mediaType;
    public String getMediaType() {return mediaType;}

    String text;
    public String getText() {return text;}

    String uin;
    public String getUin() {return uin;}

    boolean starting;
    public boolean getStarting() {return starting;}

    boolean outgoing;
    public boolean getOutgoing() {return outgoing;}
}
