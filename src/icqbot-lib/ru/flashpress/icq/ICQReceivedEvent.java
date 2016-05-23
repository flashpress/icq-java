package ru.flashpress.icq;

import org.json.simple.JSONObject;

/**
 * Created by sam on 18.05.16.
 */
public class ICQReceivedEvent
{
    ICQReceivedEvent()
    {

    }

    String eventType;
    public String getEventType() {return eventType;}

    JSONObject eventData;
    public JSONObject getEventData() {return eventData;}
}
