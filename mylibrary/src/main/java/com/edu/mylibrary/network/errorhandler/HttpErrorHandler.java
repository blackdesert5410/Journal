package com.edu.mylibrary.network.errorhandler;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.functions.Function;

public class HttpErrorHandler<T> implements Function<Throwable, Observable<T>> {
    @Override
    public Observable<T> apply(Throwable throwable) throws Exception {
        //通过这个异常处理，得到用户可以知道的原因
        return Observable.error(ExceptionHandle.handleException(throwable));
    }
}
