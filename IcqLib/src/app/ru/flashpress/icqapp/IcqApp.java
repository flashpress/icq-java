package ru.flashpress.icqapp;

import ru.flashpress.icq.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sam on 17.05.16.
 */
public class IcqApp implements IICQListener
{
    public static void main(String[] args)
    {
        String argsSrc = null;
        if (args.length > 0) {
            argsSrc = "";
            for (String s : args) argsSrc += " " + s;
        } else {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get("../config.txt"));
                argsSrc = new String(bytes);
            } catch (Exception e) {}
        }
        if (argsSrc == null) {
            System.out.println("not found file ../config.txt = -uin <UIN> -password <PASS>");
            System.exit(1);
        }
        //
        boolean debug = getBoolean(argsSrc, "debug");
        String uin = getString(argsSrc, "uin");
        String password = getString(argsSrc, "password");
        //
        IcqApp app = new IcqApp();
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

    public IcqApp()
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
            icq.sendMessage(new ICQSendMessage().setUin(message.getUin()).setMessage("pong"));
        }
    }
    public void icqReceivedEvent(ICQReceivedEvent event)
    {

    }
}
