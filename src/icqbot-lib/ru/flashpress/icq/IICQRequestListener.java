package ru.flashpress.icq;

/**
 * Created by sam on 17.05.16.
 */
@FunctionalInterface
public interface IICQRequestListener
{
    /**
     * Запрос завершен, не обьязательно успешно, чтобы получить информацию об успешности, используйте свойтва request.getStatusCode() и request.isFailed().
     * @param request
     */
    void requestCompleted(IICQRequest request);
}
