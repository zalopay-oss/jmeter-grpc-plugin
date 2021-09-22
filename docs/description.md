Name | Description | Example
--- | --- | ---
hostname | service host | 127.0.0.1
port | service port | 50051
useSsl | use ssl or not | false
keepAliveTime | keep alive time in milliseconds | 30000
keepAliveTimeout | keep alive time out in milliseconds | 30000
keepAliveWithoutCalls | send keep alive messages without calls | false
maxInboundMessageSize | max size of inbound message in bytes | 4096
packageN | package name is defined in .proto | io.grpc.examples.helloworld
service | the service is defined in .proto | Greeter
method | rpc method to load test | sayHello
request | request class name | io.grpc.examples.helloworld.HelloRequest
timeout | timeout for each call in milliseconds | 3000
metaData | gRPC interceptor | {"Authorization" : "Bearer TOKEN", "Username": "A"}
requestBuilderCode | the request message |

`Note:` The requestBuilderCode must follow the template below:

```java
import com.google.protobuf.Message;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import vn.zalopay.jmeter.grpc.utils.MessageBuilder;
// TODO: import the message request

public class RequestFactory implements MessageBuilder {
    public Message buildMessage(JMeterContext ctx) {
        // TODO: construct the message and return it
        // TIPS: you can get Jmeter variable from ctx.getVariables()
        // and Jmeter properties from ctx.getProperties()
    }
}
```
