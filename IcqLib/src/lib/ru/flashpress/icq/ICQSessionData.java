package ru.flashpress.icq;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by sam on 18.05.16.
 */
public class ICQSessionData
{
    public static enum Events {
        mchat,
        userAddedToBuddyList,
        hist,
        imState
    }
    public static final Collection<Events>EventsAll = new HashSet<>(
            Arrays.asList(
                    new Events[] {
                            Events.mchat,
                            Events.userAddedToBuddyList,
                            Events.hist,
                            Events.imState,
                            }
            )
    );

    public static enum IncludePresenceFields {
        aimId,
        friendly,
        state,
        ssl
    }
    public static final Collection<IncludePresenceFields>IncludePresenceFieldsAll = new HashSet<>(
            Arrays.asList(
                    new IncludePresenceFields[] {
                            IncludePresenceFields.aimId,
                            IncludePresenceFields.friendly,
                            IncludePresenceFields.state,
                            IncludePresenceFields.ssl,
                    }
            )
    );


    public ICQSessionData()
    {
        events = new HashSet<>();
        includePresenceFields = new HashSet<>();
    }

    public void reset()
    {
        this.view = "online";
        this.invisible = false;
        this.mobile = false;
        this.events.clear();
        this.includePresenceFields.clear();
        this.sessionTimeout = 0;
    }

    String view;
    public ICQSessionData setView(String value)
    {
        this.view = value;
        return this;
    }
    Boolean invisible;
    public ICQSessionData setInvisible(boolean value)
    {
        this.invisible = value;
        return this;
    }
    Boolean mobile;
    public ICQSessionData setMobile(boolean value)
    {
        this.mobile = value;
        return this;
    }
    HashSet<Events> events;
    public ICQSessionData setEvents(Collection<Events> value)
    {
        events.addAll(value);
        return this;
    }
    public ICQSessionData setEvent(Events value)
    {
        events.add(value);
        return this;
    }

    HashSet<IncludePresenceFields> includePresenceFields;
    public ICQSessionData setIncludePresenceFields(Collection<IncludePresenceFields> value)
    {
        includePresenceFields.addAll(value);
        return this;
    }
    public ICQSessionData setIncludePresenceField(IncludePresenceFields value)
    {
        includePresenceFields.add(value);
        return this;
    }

    long sessionTimeout;
    public ICQSessionData setSessionTimeout(long value)
    {
        this.sessionTimeout = value;
        return this;
    }

}
