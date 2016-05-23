package ru.flashpress.icq;

import org.json.simple.JSONObject;

/**
 * Created by sam on 18.05.16.
 */
public interface IICQRequest
{
    /**
     * Уникальный идентификатор каждого запроса
     * @return
     */
    int getId();

    /**
     * Код ответа, полученного от сервера, если запрос прошел успешно должно быть значение 200
     * @return
     */
    int getStatusCode();

    /**
     * Текстовое представление кода овтета, для проверки ответа лучше использовать getStatusCode().
     * @return
     */
    String getStatusText();

    /**
     * Ответ сервера на запрос
     * @return json объект
     */
    JSONObject getData();

    /**
     * Находится ли объект  в данный момент в процессе получение ответа от сервера, идет загрузка
     * @return
     */
    boolean isRunning();

    /**
     * Неудалось сделать запрос
     * @return
     */
    boolean isFailed();

    /**
     * Установить слушателя окончания запроса
     * @param listener
     */
    void addListener(IICQRequestListener listener);

    /**
     * Отменить текущий запрос
     */
    void cancel();

    /**
     * После окончания запроса, объект IICQRequest необходимо освободить вызвав этот метод,
     * в результате объект будет помещен в pool для дальнейшего использования.
     */
    void release();
}
