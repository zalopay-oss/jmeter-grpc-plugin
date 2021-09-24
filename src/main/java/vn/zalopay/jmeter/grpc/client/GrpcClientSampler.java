package vn.zalopay.jmeter.grpc.client;

import com.google.protobuf.Message;
import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;
import lombok.Getter;
import lombok.Setter;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.zalopay.jmeter.grpc.utils.Config;
import vn.zalopay.jmeter.grpc.utils.GrpcUtils;
import vn.zalopay.jmeter.grpc.utils.MessageBuilder;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class GrpcClientSampler extends AbstractSampler implements TestBean, Serializable {

  private static final long serialVersionUID = 24138743232L;
  private static final transient Logger LOGGER = LoggerFactory.getLogger(GrpcClientSampler.class);

  @Setter
  @Getter
  private String hostname = Config.HOST_NAME;
  @Setter
  @Getter
  private int port = Config.PORT;
  @Setter
  @Getter
  private boolean useSsl = Config.USE_SSL;
  @Setter
  @Getter
  private String certFile = Config.CERT_FILE;
  @Setter
  @Getter
  private String packageN = Config.PACKAGE_NAME;
  @Setter
  @Getter
  private String service = Config.SERVICE;
  @Setter
  @Getter
  private String method = Config.METHOD;
  @Setter
  @Getter
  private String request = Config.REQUEST;
  @Setter
  @Getter
  private long timeout = Config.TIME_OUT;
  @Setter
  @Getter
  private String metaData = Config.META_DATA;
  @Setter
  @Getter
  private String requestBuilderCode = Config.REQUEST_CODE;
  @Setter
  @Getter
  private boolean keepAliveWithoutCalls = Config.KEEP_ALIVE_WITHOUT_CALLS;
  @Setter
  @Getter
  private long keepAliveTime = Config.KEEP_ALIVE_TIME;
  @Setter
  @Getter
  private long keepAliveTimeout = Config.KEEP_ALIVE_TIMEOUT;
  @Setter
  @Getter
  private int maxInboundSizeMessage = Config.MAX_INBOUND_SIZE_MESSAGE;

  private transient ManagedChannel channel = null;
  private transient AbstractStub<?> blockingStub = null;
  private transient MessageBuilder messageBuilder;
  private transient Method apiMethod;

  public GrpcClientSampler() {
    setName("Grpc Client Sampler");
    LOGGER.info("Created GrpcClientSampler");
  }

  private void initGrpcClient() {
    try {
      channel = GrpcUtils.getChannel(this);
      blockingStub = GrpcUtils.getBlockingStub(this, channel);
      apiMethod = GrpcUtils.getApiMethod(this, blockingStub);
      messageBuilder = GrpcUtils.getMessageBuilder(this.getRequestBuilderCode());
    } catch (Exception e) {
      LOGGER.error("Call initGrpcClient has thrown an exception: ", e);
    }
  }

  @Override
  public SampleResult sample(Entry entry) {

    if (this.channel == null) {
      this.initGrpcClient();
    }
    // get message object to call rpc
    Message req = messageBuilder.buildMessage(JMeterContextService.getContext());
    ClientRecorder recorder = new ClientRecorder(this, req);

    recorder.recordStart();
    try {
      Object resp = apiMethod.invoke(blockingStub, req);
      recorder.recordSuccess(resp);
      LOGGER.info("Call sample has response= {}", resp);
    } catch (Exception e) {
      recorder.recordFailure(e);
      LOGGER.error("Call sample has thrown an exception: ", e);
    }

    return recorder.getResult();
  }

  @Override
  public void clear() {
    try {
      shutdown();
    } catch (InterruptedException e) {
      LOGGER.error("Call clear has thrown an InterruptedException: ", e);
      Thread.currentThread().interrupt();
    }
  }

  private void shutdown() throws InterruptedException {
    if (this.channel != null) {
      this.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
