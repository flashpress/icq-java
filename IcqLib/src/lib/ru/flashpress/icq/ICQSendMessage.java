package ru.flashpress.icq;

/**
 * Created by sam on 18.05.16.
 */
public class ICQSendMessage
{
    public ICQSendMessage()
    {

    }

    String uin;
    public ICQSendMessage setUin(String value)
    {
        this.uin = value;
        return this;
    }

    String message;
    public ICQSendMessage setMessage(String value)
    {
        this.message = value;
        return this;
    }

    boolean notifyDelivery;
    public ICQSendMessage setNotifyDelivery(boolean value)
    {
        this.notifyDelivery = value;
        return this;
    }

}
