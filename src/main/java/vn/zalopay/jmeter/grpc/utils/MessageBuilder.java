package vn.zalopay.jmeter.grpc.utils;

import com.google.protobuf.Message;
import org.apache.jmeter.threads.JMeterContext;

public interface MessageBuilder {
  Message buildMessage(JMeterContext jmCtx);
}
