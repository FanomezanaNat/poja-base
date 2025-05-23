package com.company.base.handler.exceptionHandler;

import com.company.base.handler.LambdaHandler;
import com.company.base.handler.model.ErrorModel;
import com.company.base.handler.model.ResponseEvent.LambdaUrlResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class ExceptionHandlerImpl implements ExceptionHandler<LambdaUrlResponseEvent> {
  private Logger log = LoggerFactory.getLogger(ExceptionHandlerImpl.class);

  static final String INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.toString();

  protected static final HttpHeaders HEADERS = new HttpHeaders();

  static {
    HEADERS.put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));
  }

  @Override
  public LambdaUrlResponseEvent handle(Throwable throwable) {
    log.error("Called exception handler for :", throwable);
    // adding print stack in case we have no appender or we are running inside SAM local , where
    // need the
    // output to go the stderr.
    throwable.printStackTrace();
    return new LambdaUrlResponseEvent(
        500, HEADERS.toSingleValueMap(), getErrorJson(INTERNAL_SERVER_ERROR));
  }

  protected String getErrorJson(String message) {
    try {
      return LambdaHandler.getObjectMapper().writeValueAsString(new ErrorModel(message));
    } catch (JsonProcessingException e) {
      log.error("Could not produce error JSON", e);
      return "{\"message\": \"" + message + "\" }";
    }
  }
}
