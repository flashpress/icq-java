package ru.flashpress.icq;

import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sam on 17.05.16.
 */
class ICQRequest implements FutureCallback<Content>, IICQRequest
{
    enum HttpShemes {
        HTTP {public String toString() {return "http";}},
        HTTPS {public String toString() {return "https";}},
    }

    enum HttpMethods {
        GET {public String toString() {return "GET";}},
        POST {public String toString() {return "POST";}},
    }

    enum Urls {
        CLIENT_LOGIN {public String toString() {return "api.login.icq.net/auth/clientLogin";}},
        FILE_UPLOAD {public String toString() {return "files.icq.com/files/add";}},
        FILE_GATEWAY {public String toString() {return "files.icq.com/files/init";}},
        API_ENDPOINT_START_SESSION {public String toString() {return "api.icq.net/aim/startSession";}},
        API_ENDPOINT_END_SESSION {public String toString() {return "api.icq.net/aim/endSession";}},
        API_ENDPOINT_SEND_MESSAGE {public String toString() {return "api.icq.net/im/sendIM";}},
        API_ENDPOINT_ADD_USER {public String toString() {return "api.icq.net/buddylist/addBuddy";}},
        API_ENDPOINT_GET_PRESENCE {public String toString() {return "api.icq.net/presence/get";}},
        API_ENDPOINT_SET_STATE {public String toString() {return "api.icq.net/presence/setState";}},
        API_ENDPOINT_UPDATE_MEMBERS_DIR {public String toString() {return "api.icq.net/memberDir/update";}},
    }

    private static ArrayList<ICQRequest> pool = new ArrayList<>();
    static ICQRequest create(HttpMethods httpMethod, HttpShemes sheme, Urls host)
    {
        ICQRequest request;
        if (!pool.isEmpty()) {
            request = pool.remove(0);
        } else {
            request = new ICQRequest();
        }
        request.start(httpMethod, sheme, host);
        return request;
    }
    static ICQRequest create(HttpMethods httpMethod, String url)
    {
        ICQRequest request;
        if (!pool.isEmpty()) {
            request = pool.remove(0);
        } else {
            request = new ICQRequest();
        }
        request.start(httpMethod, url);
        return request;
    }


    private URIBuilder builder;
    private ArrayList<IICQRequestListener> listeners;
    HashMap<String, String> parametersMap;
    private ICQRequest()
    {
        builder = new URIBuilder();
        listeners = new ArrayList<>();
        parametersMap = new HashMap<>();
    }

    private void clear()
    {
        running = false;
        _isFailed = false;
        statusCode = 0;
        statusText = null;
        data = null;
        builder.clearParameters();
        listeners.clear();
        parametersMap.clear();
    }

    private static int idCount = 0;
    private int id;

    HttpMethods httpMethod;
    String url;
    private void start(HttpMethods httpMethod, HttpShemes sheme, Urls host)
    {
        this.id = ++idCount;
        this.httpMethod = httpMethod;
        this.url = sheme.toString()+"://"+host.toString();
        clear();
        //
        builder.setScheme(sheme.toString());
        builder.setHost(host.toString());
    }

    private Pattern patternUrl = Pattern.compile("^(?<sheme>http(s)?)://(?<host>.+)$");
    private void start(HttpMethods httpMethod, String url)
    {
        this.id = ++idCount;
        this.httpMethod = httpMethod;
        this.url = url;
        clear();
        //
        Matcher m = patternUrl.matcher(url);
        if (m.find()) {
            builder.setScheme(m.group("sheme"));
            builder.setHost(m.group("host"));
        }
    }

    void setParameter(String key, String value)
    {
        builder.setParameter(key, value);
        parametersMap.put(key, value);
    }

    private Request request;
    Future<Content> future;
    void run()
    {
        running = true;
        URI requestURL = null;
        try {
            requestURL = builder.build();
        } catch (Exception e){

        }
        ICQDebug.out("-------> request["+id+"]: ", requestURL);
        //
        ExecutorService threadpool = Executors.newFixedThreadPool(2);
        Async async = Async.newInstance().use(threadpool);
        switch (httpMethod) {
            case POST:
                request = Request.Post(requestURL);
                break;
            case GET:
                request = Request.Get(requestURL);
                break;
        }

        future = async.execute(request, this);
    }

    public void cancel()
    {
        if (running && future != null) {
            try {
                future.cancel(true);
                future = null;
            } catch (Exception e) {

            }
        }
        running = false;
    }

    public void addListener(IICQRequestListener listener)
    {
        listeners.add(listener);
    }

    public int getId() {return this.id;}

    private int statusCode;
    public int getStatusCode() {return this.statusCode;}

    private String statusText;
    public String getStatusText() {return this.statusText;}

    private JSONObject data;
    public JSONObject getData() {return this.data;}

    private boolean running;
    public boolean isRunning() {return running;}

    private boolean _isFailed;
    public boolean isFailed() {return _isFailed;}

    public void completed(Content content)
    {
        ICQDebug.out("<------- answer["+id+"]:", content.asString());
        //
        running = false;
        //
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;
        try {
            jsonObject = (JSONObject)jsonParser.parse(content.asString());
            jsonObject = (JSONObject)jsonObject.get("response");
            statusCode = Integer.parseInt(jsonObject.get("statusCode").toString());
            statusText = jsonObject.get("statusText").toString();
            data = (JSONObject)jsonObject.get("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //
        for (IICQRequestListener l : listeners) l.requestCompleted(this);
    }
    public void failed(Exception e)
    {
        ICQDebug.out("Request failed:", e.getMessage(), request);
        statusCode = 0;
        running = false;
        _isFailed = true;
        for (IICQRequestListener l : listeners) l.requestCompleted(this);
    }
    public void cancelled()
    {
        ICQDebug.out("Request cancelled: ", request);
        statusCode = 0;
        running = false;
    }

    public void release()
    {
        this.clear();
        if (running) cancel();
        if (!pool.contains(this)) pool.add(this);
    }

    public String toString()
    {
        return "[ICQRequest id:"+this.id+", url:"+this.url+"]";
    }
}
