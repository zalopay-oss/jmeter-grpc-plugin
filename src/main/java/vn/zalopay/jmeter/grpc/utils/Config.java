package vn.zalopay.jmeter.grpc.utils;

public class Config {
  private Config() {
  }

  private static final PropUtils props = new PropUtils();

  public static final String HOST_NAME = props.getOrDefault("sampler.host", "localhost");
  public static final int PORT = Integer.parseInt(props.getOrDefault("sampler.port", "50051"));
  public static final boolean USE_SSL = Boolean.getBoolean(props.getOrDefault("sampler.use.ssl", "false"));
  public static final String CERT_FILE = props.getOrDefault("sampler.service.cert.file", "");
  public static final String PACKAGE_NAME = props.getOrDefault("sampler.package.name", "io.grpc.examples.helloworld");
  public static final String SERVICE = props.getOrDefault("sampler.service.name", "Greeter");
  public static final String METHOD = props.getOrDefault("sampler.method.name", "sayHello");
  public static final String REQUEST = props.getOrDefault("sampler.request",
      "io.grpc.examples.helloworld.HelloRequest");
  public static final String META_DATA = props.getOrDefault("sampler.metadata", "{}");
  public static final long TIME_OUT = Long.parseLong(props.getOrDefault("sampler.timeout", "3000"));
  public static final String REQUEST_CODE = props.getOrDefault("sampler.request.code",
      "import vn.zalopay.jmeter.grpc.utils.MessageBuilder;\n" + "import io.grpc.examples.helloworld.HelloRequest;\n"
          + "import com.google.protobuf.Message;\n" + "import org.apache.jmeter.threads.JMeterContext;\n"
          + "import org.apache.jmeter.threads.JMeterVariables;\n\n"
          + "public class RequestFactory implements MessageBuilder {\n\n"
          + "    public Message buildMessage(JMeterContext ctx) {\n\n"
          + "        JMeterVariables vars = ctx.getVariables();\n"
          + "        HelloRequest request = HelloRequest.newBuilder().setName(vars.get(\"Name\")).build();\n"
          + "        return request;\n" + "    }\n" + "}\n");
}
