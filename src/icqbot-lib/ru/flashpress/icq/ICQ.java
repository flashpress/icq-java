package ru.flashpress.icq;


import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

/**
 * Created by sam on 17.05.16.
 */
public class ICQ implements ActionListener
{
    private String uin;
    private String password;
    private String token;
    //
    private HashSet<IICQListener> listeners;

    /**
     * Конструктор
     * @param uin Ваш icq uin номер
     * @param password Пароль от вашего uin
     * @param token Константа
     */
    public ICQ(String uin, String password, String token)
    {
        this.uin = uin;
        this.password = password;
        this.token = token;
        //
        listeners = new HashSet<>();
    }

    private void signedRequest(ICQRequest request)
    {
        request.setParameter("r", getTime());
        request.setParameter("a", this.a);
        request.setParameter("ts", String.valueOf(this.ts));
        request.setParameter("k", this.token);
        request.setParameter("f", "JSON");
        request.setParameter("sig_sha256", Crypto.sigsha(request, sessionKey));
    }
    private void apiRequest(ICQRequest request)
    {
        request.setParameter("r", getTime());
        request.setParameter("f", "JSON");
        request.setParameter("aimsid", aimsid);
    }

    private String a;
    private long ts;
    private String secret;
    private String sessionKey;
    private void connectComplete(ICQRequest request)
    {
        if (request.getStatusCode() == 200 && !request.isFailed()) {
            try {
                JSONObject data = request.getData();
                JSONObject token = (JSONObject)request.getData().get("token");
                this.a = token.get("a").toString();
                this.ts = Long.parseLong(data.get("hostTime").toString());
                this.secret = data.get("sessionSecret").toString();
                this.sessionKey = Crypto.sha(secret, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //
        for (IICQListener l : listeners) l.icqConnected(request);
    }

    private String aimsid;
    private String fetchUrl;
    private int timeToNextFetch = 30;
    private void startComplete(ICQRequest request)
    {
        if (request.getStatusCode() == 200 && !request.isFailed()) {
            try {
                JSONObject data = request.getData();
                this.aimsid = data.get("aimsid").toString();
                this.fetchUrl = data.get("fetchBaseURL").toString();
                this.timeToNextFetch = data.containsKey("timeToNextFetch") ? Integer.parseInt(data.get("timeToNextFetch").toString()) : 10;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (IICQListener l : listeners) l.icqStarted(request);
    }

    private Timer timer;
    private void restartTimeout()
    {
        if (timer == null) {
            timer = new Timer(0, this);
        } else {
            timer.stop();
        }
        timer.setDelay(timeToNextFetch*1000);
        timer.start();
    }
    public void actionPerformed(ActionEvent e)
    {
        restartTimeout();
        fetchEvent();
    }

    private ICQRequest fetchRequest;
    private void fetchEvent()
    {
        if (this.fetchRequest != null) {
            this.fetchRequest.release();
        }
        //
        fetchRequest = ICQRequest.create(ICQRequest.HttpMethods.GET, this.fetchUrl);
        fetchRequest.addListener((request)-> fetchComplete((ICQRequest) request));
        fetchRequest.setParameter("r", getTime());
        fetchRequest.setParameter("f", "JSON");
        fetchRequest.setParameter("peek", "0");
        fetchRequest.setParameter("timeout", fetchTimeoutStr);
        fetchRequest.run();
    }
    private void fetchComplete(ICQRequest request)
    {
        if (request.getStatusCode() == 200 && !request.isFailed()) {
            JSONObject data = request.getData();
            this.fetchUrl = data.get("fetchBaseURL").toString();
            this.timeToNextFetch = Integer.parseInt(data.get("timeToNextFetch").toString());
            //
            try {
                JSONArray events = (JSONArray) data.get("events");
                JSONObject event;
                String eventType;
                JSONObject eventData;
                JSONArray messages;
                JSONObject messageData;
                String messageUin;
                boolean messageStarting;
                ICQReceivedMessage receivedMessage;
                ICQReceivedEvent receivedEvent;
                for (int i = 0; i < events.size(); i++) {
                    event = (JSONObject) events.get(i);
                    eventType = event.get("type").toString();
                    eventData = (JSONObject) event.get("eventData");
                    //
                    if (eventType.equals("histDlgState")) {
                        messageUin = eventData.get("sn").toString();
                        messageStarting = getBoolParameter(eventData.get("starting"));
                        //
                        messages = (JSONArray) eventData.get("messages");
                        if (messages != null) {
                            for (int j = 0; j < messages.size(); j++) {
                                messageData = (JSONObject) messages.get(j);
                                receivedMessage = new ICQReceivedMessage();
                                receivedMessage.eventType = eventType;
                                receivedMessage.eventData = eventData;
                                receivedMessage.uin = messageUin;
                                receivedMessage.text = messageData.get("text").toString();
                                receivedMessage.starting = messageStarting;
                                receivedMessage.outgoing = getBoolParameter(messageData.get("outgoing"));
                                for (IICQListener l : listeners) l.icqReceivedMessage(receivedMessage);
                            }
                        }
                    } else {
                        receivedEvent = new ICQReceivedEvent();
                        receivedEvent.eventType = eventType;
                        receivedEvent.eventData = eventData;
                        for (IICQListener l : listeners) l.icqReceivedEvent(receivedEvent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        fetchEvent();
    }

    private static boolean getBoolParameter(Object o)
    {
        String str = o != null ? o.toString() : "";
        return str.equals("true");
    }

    private static String getTime()
    {
        long time = System.currentTimeMillis();
        time = Long.parseLong("" + String.valueOf(time));
        return String.valueOf(time);
    }


    // public API --------------------------------------------------------------------------------

    /**
     * Зарегистрировать слушателя событий, необходим для получения события о новом сообщении IICQListener::icqReceivedMessage
     * @param listener
     */
    public void addListener(IICQListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Установить соединение с сервером
     * @return Объект IICQRequest, который необходимо освободить после завершения, вызвав метод release().
     */
    public IICQRequest connect()
    {
        ICQRequest connectRequest = ICQRequest.create(ICQRequest.HttpMethods.POST, ICQRequest.HttpShemes.HTTPS, ICQRequest.Urls.CLIENT_LOGIN);
        connectRequest.addListener((request)->connectComplete((ICQRequest)request));
        connectRequest.setParameter("s", this.uin);
        connectRequest.setParameter("pwd", this.password);
        connectRequest.setParameter("k", this.token);
        connectRequest.setParameter("devId", this.token);
        connectRequest.setParameter("tokenType", "longterm");
        connectRequest.setParameter("idType", "ICQ");
        connectRequest.setParameter("f", "JSON");
        connectRequest.run();
        return connectRequest;
    }

    /**
     * Начать сессию.
     * @param sessionData Информация о текущей сессии
     * @return Объект IICQRequest, который необходимо освободить после завершения, вызвав метод release().
     */
    public ICQRequest startSession(ICQSessionData sessionData)
    {
        ICQRequest startRequest = ICQRequest.create(ICQRequest.HttpMethods.GET, ICQRequest.HttpShemes.HTTP, ICQRequest.Urls.API_ENDPOINT_START_SESSION);
        startRequest.addListener((request)->startComplete((ICQRequest)request));
        startRequest.setParameter("view", sessionData.view);
        startRequest.setParameter("invisible", String.valueOf(sessionData.invisible));
        startRequest.setParameter("mobile", !sessionData.mobile ? "0" : "1");
        startRequest.setParameter("sessionTimeout", String.valueOf(sessionData.sessionTimeout));
        startRequest.setParameter("events", StringUtils.join(sessionData.events, ","));
        startRequest.setParameter("includePresenceFields", StringUtils.join(sessionData.includePresenceFields, ","));
        signedRequest(startRequest);
        startRequest.run();
        return startRequest;
    }

    private boolean fetching;

    /**
     * Прослушивает ли приложение сервер в данный момент. Если false - вызовите метод startFetch().
     * @return
     */
    public boolean isFetching() {return fetching;}

    private String fetchTimeoutStr;

    /**
     * Начать прослушивать сервер на наличие новых сообщений. Устанавливает свойство isFetching в значение false
     * @param fetchTimeout Время в миллисекундах, через которое приложение закрывает текущее long-poll соединение и открывает новое.
     */
    public void startFetch(int fetchTimeout)
    {
        this.fetchTimeoutStr = String.valueOf(fetchTimeout);
        this.fetching = true;
        fetchEvent();
    }

    /**
     * Начать прослушивать сервер на наличие новых сообщений c таймаутом 30 сек. Устанавливает свойство isFetching в значение false
     */
    public void startFetch()
    {
        this.startFetch(30000);
    }

    /**
     * Остановить прослушшивание сервера на наличие новых сообщений. Устанавливает свойство isFetching в значение false
     */
    public void stopFetch()
    {
        this.fetching = false;
        if (timer != null) {
            timer.stop();
        }
        if (fetchRequest != null) {
            fetchRequest.release();
            fetchRequest = null;
        }
    }

    /**
     * Разоравать соединение с сервером.
     * @return
     */
    public ICQRequest disconnect()
    {
        ICQRequest disconnectRequest = ICQRequest.create(ICQRequest.HttpMethods.GET, ICQRequest.HttpShemes.HTTP, ICQRequest.Urls.API_ENDPOINT_END_SESSION);
        disconnectRequest.addListener((request) -> {
            fetchUrl = null;
            aimsid = null;
            request.release();
        });
        apiRequest(disconnectRequest);
        disconnectRequest.run();
        return disconnectRequest;
    }

    /**
     * Установить текущее состояние клиента online/offline
     * @param state Может принимать одно из значений {online, offline}
     * @return Объект IICQRequest, который необходимо освободить после завершения, вызвав метод release().
     */
    public IICQRequest setState(String state)
    {
        ICQRequest request = ICQRequest.create(ICQRequest.HttpMethods.GET, ICQRequest.HttpShemes.HTTP, ICQRequest.Urls.API_ENDPOINT_SET_STATE);
        request.setParameter("view", state);
        apiRequest(request);
        request.run();
        return request;
    }

    /**
     * Отправить сообщение собеседнику
     * @param message Сообщение которое необходимо отправить
     * @return Объект IICQRequest, который необходимо освободить после завершения, вызвав метод release().
     */
    public IICQRequest sendMessage(ICQSendMessage message)
    {
        ICQRequest request = ICQRequest.create(ICQRequest.HttpMethods.POST, ICQRequest.HttpShemes.HTTP, ICQRequest.Urls.API_ENDPOINT_SEND_MESSAGE);
        request.setParameter("t", message.uin);
        request.setParameter("message", message.text);
        request.setParameter("notifyDelivery", message.notifyDelivery?"1":"0");
        apiRequest(request);
        request.run();
        return request;
    }

}
