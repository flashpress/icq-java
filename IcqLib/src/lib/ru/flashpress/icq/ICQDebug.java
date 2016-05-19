package ru.flashpress.icq;

import java.util.Date;

/**
 * Created by sam on 19.05.16.
 */
public class ICQDebug
{
    private static boolean active;
    public static void init(boolean active)
    {
        ICQDebug.active = active;
        ICQDebug.outBuilder = new StringBuilder();
    }

    private static void prepareMessage()
    {
        Date d = new Date();
        outBuilder.append("[icq] ("+d.toString()+" "+(System.currentTimeMillis()%1000)+") ");
    }

    private static StringBuilder outBuilder;
    public static void out(Object... arguments)
    {
        if (!active) return;
        //
        outBuilder.delete(0, outBuilder.length());
        prepareMessage();
        for (int i=0; i<arguments.length; i++) outBuilder.append(" "+arguments[i]);
        System.out.println(outBuilder.toString());
    }
}
