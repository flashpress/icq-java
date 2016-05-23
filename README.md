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
        IICQRequest connectRequest = icq.connect();
        connectRequest.addListener(this::onConnected);
    }
    private void onConnected(IICQRequest request)
    {
        if (request.getStatusCode() == 200) {
            ICQSessionData session = new ICQSessionData()
                    .setView("online")
                    .setInvisible(false)
                    .setMobile(false)
                    .setSessionTimeout(2592000)
                    .setEvents(ICQSessionData.EventsAll)
                    .setIncludePresenceFields(ICQSessionData.IncludePresenceFieldsAll);
            IICQRequest startRequest = icq.startSession(session);
            startRequest.addListener(this::onStartedSession);
        }
        request.release();
    }
    private void onStartedSession(IICQRequest request)
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
            IICQRequest request = icq.sendMessage(new ICQSendMessage().setUin(message.getUin()).setText("pong"));
            request.addListener(this::onSendMessage);
        }
    }
    private void onSendMessage(IICQRequest request)
    {
        ICQDebug.out("onSendMessage = " + request);
        request.release();
    }
}


```
