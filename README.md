# Build and Run
Create `run.config` file with content:
```
-debug -uin <your_uin> -password <your_password>
```
and run:
```
./gradlew run
```

# Ping-Pong bot
```Java
import ru.flashpress.icq.*;


/**
 * Created by sam on 17.05.16.
 */
public class PingPongApp implements IICQListener
{
    public static void main(String[] args)
    {
        boolean debug = true;
        String uin = "your uin";
        String password = "your password";
        //
        PingPongApp app = new PingPongApp();
        app.start(debug, uin, password);
    }

    public PingPongApp()
    {

    }

    private ICQ icq;
    private void start(boolean debug, String uin, String password)
    {
        if (debug) ICQDebug.init(true);
        //
        icq = new ICQ(uin, password, "ic17mFHiwr52TKrx");
        icq.addListener(this);
        icq.connect();
    }

    public void icqConnected(IICQRequest request)
    {
        if (request.getStatusCode() == 200) {
            ICQSessionData session = new ICQSessionData()
                    .setView("online")
                    .setInvisible(false)
                    .setMobile(false)
                    .setSessionTimeout(2592000)
                    .setEvents(ICQSessionData.EventsAll)
                    .setIncludePresenceFields(ICQSessionData.IncludePresenceFieldsAll);
            icq.startSession(session);
        }
        request.release();
    }
    public void icqStarted(IICQRequest request)
    {
        if (request.getStatusCode() == 200) {
            icq.startFetch();
            icq.setState("online");
        }
        request.release();
    }

    public void icqReceivedMessage(ICQReceivedMessage message)
    {
        if (message.getText().equalsIgnoreCase("ping")) {
            ICQSendMessage answer = new ICQSendMessage();
            answer.setUin(message.getUin());
            answer.setMessage("pong");
            icq.sendMessage(answer);
        }
    }
    public void icqReceivedEvent(ICQReceivedEvent event)
    {

    }
}

```
