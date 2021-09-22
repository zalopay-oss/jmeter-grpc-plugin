package vn.zalopay.jmeter.grpc.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.AbstractStub;
import io.netty.handler.ssl.SslContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.zalopay.jmeter.grpc.client.GrpcClientInterceptor;
import vn.zalopay.jmeter.grpc.client.GrpcClientSampler;
import vn.zalopay.jmeter.grpc.compiler.StringGeneratedJavaCompilerFacade;

import javax.net.ssl.SSLException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GrpcUtils {

  private GrpcUtils() {
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(GrpcUtils.class);

  private static final transient ClassLoader classLoader = GrpcUtils.class.getClassLoader();

  public static final String OK = "OK";
  public static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";
  public static final String FAILED_PRECONDITION = "FAILED_PRECONDITION";
  public static final String OUT_OF_RANGE = "OUT_OF_RANGE";
  public static final String UNAUTHENTICATED = "UNAUTHENTICATED";
  public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
  public static final String NOT_FOUND = "NOT_FOUND";
  public static final String ABORTED = "ABORTED";
  public static final String ALREADY_EXISTS = "ALREADY_EXISTS";
  public static final String RESOURCE_EXHAUSTED = "RESOURCE_EXHAUSTED";
  public static final String CANCELLED = "CANCELLED";
  public static final String DATA_LOSS = "DATA_LOSS";
  public static final String UNKNOWN = "UNKNOWN";
  public static final String INTERNAL = "INTERNAL";
  public static final String UNIMPLEMENTED = "UNIMPLEMENTED";
  public static final String UNAVAILABLE = "UNAVAILABLE";
  public static final String DEADLINE_EXCEEDED = "DEADLINE_EXCEEDED";

  public static final String CODE_200 = "200";
  public static final String CODE_400 = "400";
  public static final String CODE_401 = "401";
  public static final String CODE_403 = "403";
  public static final String CODE_404 = "404";
  public static final String CODE_409 = "409";
  public static final String CODE_429 = "429";
  public static final String CODE_499 = "499";
  public static final String CODE_500 = "500";
  public static final String CODE_501 = "501";
  public static final String CODE_503 = "503";
  public static final String CODE_504 = "504";

  public static final String NEW_BLOCKING_STUB = "newBlockingStub";
  public static final String BUILD_MESSAGE_CLASS_NAME = "RequestFactory";

  public static final Map<String, String> statusCodeMap;

  // reference https://cloud.google.com/apis/design/errors#handling_errors
  static {
    Map<String, String> map = new HashMap<>(20);
    map.put(OK, CODE_200);
    map.put(INVALID_ARGUMENT, CODE_400);
    map.put(FAILED_PRECONDITION, CODE_400);
    map.put(OUT_OF_RANGE, CODE_400);
    map.put(UNAUTHENTICATED, CODE_401);
    map.put(PERMISSION_DENIED, CODE_403);
    map.put(NOT_FOUND, CODE_404);
    map.put(ABORTED, CODE_409);
    map.put(ALREADY_EXISTS, CODE_409);
    map.put(RESOURCE_EXHAUSTED, CODE_429);
    map.put(CANCELLED, CODE_499);
    map.put(DATA_LOSS, CODE_500);
    map.put(UNKNOWN, CODE_500);
    map.put(INTERNAL, CODE_500);
    map.put(UNIMPLEMENTED, CODE_501);
    map.put(UNAVAILABLE, CODE_503);
    map.put(DEADLINE_EXCEEDED, CODE_504);

    statusCodeMap = Collections.unmodifiableMap(map);
  }

  // reference io.grpc.Status.Code
  protected static final List<String> statusCodeList = Arrays.asList(OK, CANCELLED, UNKNOWN, INVALID_ARGUMENT,
      DEADLINE_EXCEEDED, NOT_FOUND, ALREADY_EXISTS, PERMISSION_DENIED, RESOURCE_EXHAUSTED, FAILED_PRECONDITION, ABORTED,
      OUT_OF_RANGE, UNIMPLEMENTED, INTERNAL, UNAVAILABLE, DATA_LOSS, UNAUTHENTICATED);

  public static String getStatusCode(int index) {
    return statusCodeList.get(index);
  }

  public static String getStatusCodeNumber(String code) {
    return statusCodeMap.getOrDefault(code, UNKNOWN);
  }

  private static boolean isBlankString(String str) {
    return str == null || str.trim().isEmpty();
  }

  private static String decapitalize(String str) {
    int strLen;
    return str != null && (strLen = str.length()) != 0
        ? (new StringBuffer(strLen)).append(Character.toLowerCase(str.charAt(0))).append(str.substring(1)).toString()
        : str;
  }

  public static Map createHeaderMap(String metaData) {
    Map headerMap = new HashMap<>();
    if (!isBlankString(metaData)) {
      try {
        headerMap = new ObjectMapper().readValue(metaData, Map.class);
      } catch (JsonProcessingException e) {
        LOGGER.error("Failed to load metadata: ", e);
      }
    }
    return headerMap;
  }

  public static String getGrpcServiceClass(GrpcClientSampler sampler) {
    return sampler.getPackageN() + "." + sampler.getService() + "Grpc";
  }

  public static String getSamplerData(GrpcClientSampler sampler, String req) {
    return sampler.getHostname() + ":" + sampler.getPort() + "\n" + sampler.getService() + "#" + sampler.getMethod()
        + "\nRequestData:\n" + req;
  }

  public static String getJsonMessageResponse(Object resp) {
    try {
      return JsonFormat.printer().preservingProtoFieldNames().includingDefaultValueFields().print((Message) resp);
    } catch (InvalidProtocolBufferException e) {
      LOGGER.error("call getJsonMessageResponse has thrown an Exception: ", e);
      return "";
    }
  }

  public static ManagedChannel getChannel(GrpcClientSampler sampler) throws SSLException {
    Map<String, String> headerMap = createHeaderMap(sampler.getMetaData());

    ManagedChannelBuilder builder = NettyChannelBuilder.forAddress(sampler.getHostname(), sampler.getPort())
        .keepAliveWithoutCalls(sampler.isKeepAliveWithoutCalls())
        .intercept(new GrpcClientInterceptor(headerMap, sampler.getTimeout()));

    if (sampler.getKeepAliveTime() >= 0) {
      builder.keepAliveTime(sampler.getKeepAliveTime(), TimeUnit.MILLISECONDS);
    }

    if (sampler.getKeepAliveTimeout() >= 0) {
      builder.keepAliveTimeout(sampler.getKeepAliveTimeout(), TimeUnit.MILLISECONDS);
    }

    if (sampler.getMaxInboundSizeMessage() >= 0) {
      builder.maxInboundMessageSize(sampler.getMaxInboundSizeMessage());
    }

    if (!sampler.isUseSsl()) {
      builder = builder.usePlaintext();
    } else if (!StringUtils.isBlank(sampler.getCertFile())) {
      // build out a managed channel that can accept the ssl cert file
      // Get the file and verify it exists
      File certFile = new File(sampler.getCertFile());
      if (!certFile.exists()) {
        LOGGER.error("The cert file passed in does not exist at: ", sampler.getCertFile());
      }

      // build the client side ssl context with the cert
      SslContext sslContext = GrpcSslContexts.forClient().trustManager(certFile).build();

      // add the ssl context to the builder
      builder = ((NettyChannelBuilder) builder).sslContext(sslContext);
    }
    return builder.build();
  }

  public static AbstractStub<?> getBlockingStub(GrpcClientSampler sampler, Channel channel)
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> serviceGrpcClass = classLoader.loadClass(GrpcUtils.getGrpcServiceClass(sampler));
    Method newBlockingStubMethod = serviceGrpcClass.getMethod(GrpcUtils.NEW_BLOCKING_STUB, Channel.class);
    return (AbstractStub<?>) newBlockingStubMethod.invoke(null, channel);
  }

  public static Method getApiMethod(GrpcClientSampler sampler, AbstractStub<?> blockingStub)
      throws ClassNotFoundException, NoSuchMethodException {
    String methodName = decapitalize(sampler.getMethod());
    Method apiMethod = blockingStub.getClass().getMethod(methodName, Class.forName(sampler.getRequest()));
    apiMethod.setAccessible(true);
    return apiMethod;
  }

  public static MessageBuilder getMessageBuilder(String source) {
    try {
      StringGeneratedJavaCompilerFacade compilerFacade = new StringGeneratedJavaCompilerFacade(classLoader);
      Class<? extends MessageBuilder> compiledClass = compilerFacade.compile(BUILD_MESSAGE_CLASS_NAME, source,
          MessageBuilder.class);
      return compiledClass.newInstance();
    } catch (Exception e) {
      LOGGER.error("Failed to getMessageBuilder: ", e);
      throw new IllegalStateException("The generated class (" + BUILD_MESSAGE_CLASS_NAME + ") failed to instantiate.",
          e);
    }
  }
}
