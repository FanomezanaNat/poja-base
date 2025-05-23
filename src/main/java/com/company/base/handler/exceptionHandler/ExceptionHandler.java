package com.company.base.handler.exceptionHandler;

public interface ExceptionHandler<R> {
  R handle(Throwable throwable);
}
