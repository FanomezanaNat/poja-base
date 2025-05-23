package com.company.base.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.company.base.PojaApplication;
import com.company.base.handler.exceptionHandler.ExceptionHandler;
import com.company.base.handler.exceptionHandler.ExceptionHandlerImpl;
import com.company.base.handler.model.ResponseEvent.LambdaUrlResponseEvent;
import com.company.base.handler.model.requestEvent.LambdaUrlRequestEvent;
import com.company.base.handler.model.wrapper.HttpServletRequestWrapper;
import com.company.base.handler.model.wrapper.HttpServletResponseWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

public class LambdaHandler
    implements RequestHandler<LambdaUrlRequestEvent, LambdaUrlResponseEvent> {

  private static ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  private final ConfigurableApplicationContext context;
  private final RequestMappingHandlerAdapter handlerAdapter;
  private final RequestMappingHandlerMapping handlerMapping;
  private final ExceptionHandler<LambdaUrlResponseEvent> exceptionHandler;

  public LambdaHandler() {
    this.context = applicationContext();
    this.handlerAdapter = context.getBean(RequestMappingHandlerAdapter.class);
    this.handlerMapping = context.getBean(RequestMappingHandlerMapping.class);
    this.exceptionHandler = defaultExceptionHandler();
  }

  @Override
  public LambdaUrlResponseEvent handleRequest(LambdaUrlRequestEvent event, Context context) {
    try {
      HttpServletRequest request = new HttpServletRequestWrapper(event);
      ServletRequestPathUtils.parseAndCache(request);

      Map<String, List<String>> headers = toMultiValueHeaders(event.getHeaders());

      ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
      HttpServletResponseWrapper response =
          new HttpServletResponseWrapper(responseOutputStream, headers);

      HandlerExecutionChain executionChain = handlerMapping.getHandler(request);
      if (executionChain == null) {
        throw new RuntimeException("No handler found for request " + request.getRequestURI());
      }

      Object handler = executionChain.getHandler();
      handlerAdapter.handle(request, response, handler);

      String responseBody = responseOutputStream.toString(StandardCharsets.UTF_8);
      return new LambdaUrlResponseEvent(
          response.getStatus(), flattenHeaders(headers), responseBody);

    } catch (Exception e) {
      return exceptionHandler.handle(e);
    }
  }

  private ExceptionHandler<LambdaUrlResponseEvent> defaultExceptionHandler() {
    return new ExceptionHandlerImpl();
  }
  ;

  private ConfigurableApplicationContext applicationContext() {
    SpringApplication application = new SpringApplication(PojaApplication.class);
    application.setDefaultProperties(Map.of("server.port", "0"));
    return application.run();
  }

  private Map<String, String> flattenHeaders(Map<String, List<String>> headers) {
    Map<String, String> flat = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
      flat.put(entry.getKey(), String.join(",", entry.getValue()));
    }
    return flat;
  }

  private Map<String, List<String>> toMultiValueHeaders(Map<String, String> singleValueHeaders) {
    Map<String, List<String>> headers = new HashMap<>();
    if (singleValueHeaders != null) {
      singleValueHeaders.forEach((key, value) -> headers.put(key, List.of(value)));
    }
    return headers;
  }

  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
