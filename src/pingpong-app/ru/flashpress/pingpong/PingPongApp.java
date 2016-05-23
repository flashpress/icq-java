package ru.flashpress.pingpong;

import ru.flashpress.icq.*;



import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sam on 17.05.16.
 */
public class PingPongApp implements IICQListener
{
    public static void main(String[] args)
    {
        Path currentRelativePath = Paths.get("");
        System.out.println("Current relative path is: " + currentRelativePath.toAbsolutePath().toString());
        //
        String argsSrc = null;
        if (args.length > 0) {
            argsSrc = "";
            for (String s : args) argsSrc += " " + s;
        } else {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get("./run.config"));
                argsSrc = new String(bytes);
            } catch (Exception e) {}
        }
        if (argsSrc == null) {
            System.out.println("not found file ./run.config = -uin <UIN> -password <PASS>");
            System.exit(1);
        }
        //
        boolean debug = getBoolean(argsSrc, "debug");
        String uin = getString(argsSrc, "uin");
        String password = getString(argsSrc, "password");
        //
        PingPongApp app = new PingPongApp();
        app.start(debug, uin, password);
    }
    private static String getString(String source, String name)
    {
        Pattern p = Pattern.compile("-"+name+"\\s+(?<value>[^\\s]+)");
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group("value");
        } else {
            return null;
        }
    }
    private static boolean getBoolean(String source, String name)
    {
        Pattern p = Pattern.compile("-"+name);
        Matcher m = p.matcher(source);
        return m.find();
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
