package ru.flashpress.icq;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by sam on 17.05.16.
 */
class Crypto
{
    public static String sha(String msg, String key)
    {
        try {
            Mac m = Mac.getInstance("HMACSHA256");
            m.init(new SecretKeySpec(key.getBytes(), "HMACSHA256"));
            m.update(msg.getBytes());
            return new String(java.util.Base64.getEncoder().encode(m.doFinal()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String encode(String s)
    {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getSortedParameters(HashMap<String, String> parametersMap)
    {
        SortedSet<String> keys = new TreeSet<>(parametersMap.keySet());
        StringBuilder sorted = new StringBuilder();
        for (String key : keys) {
            if (sorted.length() != 0) sorted.append("&");
            sorted.append(key).append("=").append(encode(parametersMap.get(key)));
        }
        return sorted.toString();
    }

    public static String sigsha(String url, String httpMethod, String sortedParameters, String sessionKey)
    {
        String fullRequest = httpMethod + "&" +
                encode(url) + "&" +
                encode(sortedParameters);
        return sha(fullRequest, sessionKey);
    }

    public static String sigsha(ICQRequest request, String sessionKey)
    {
        return sigsha(request.url, request.httpMethod.toString(), getSortedParameters(request.parametersMap), sessionKey);
    }
}
