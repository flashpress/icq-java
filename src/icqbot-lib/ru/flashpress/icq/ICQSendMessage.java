package ru.flashpress.icq;

/**
 * Класс для отправки сообщения собеседнику
 * Created by sam on 18.05.16.
 */
public class ICQSendMessage
{
    public ICQSendMessage()
    {

    }

    String uin;

    /**
     * UIN номер кому адресовано сообщение
     * @param value uin номер
     * @return
     */
    public ICQSendMessage setUin(String value)
    {
        this.uin = value;
        return this;
    }

    String text;
    /**
     * Установить текст сообщения
     * @param value Текст сообщения
     * @return
     */
    public ICQSendMessage setText(String value)
    {
        this.text = value;
        return this;
    }

    boolean notifyDelivery;
    public ICQSendMessage setNotifyDelivery(boolean value)
    {
        this.notifyDelivery = value;
        return this;
    }

}
