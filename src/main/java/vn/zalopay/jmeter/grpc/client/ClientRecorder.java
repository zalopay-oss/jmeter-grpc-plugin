package vn.zalopay.jmeter.grpc.client;

import com.google.protobuf.Message;
import vn.zalopay.jmeter.grpc.utils.GrpcUtils;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.apache.jmeter.samplers.SampleResult;

public class ClientRecorder {

  private SampleResult result;

  public ClientRecorder(GrpcClientSampler sampler, Message req) {
    String samplerData = GrpcUtils.getSamplerData(sampler, req.toString());
    result = new SampleResult();
    result.setSampleLabel(sampler.getName());
    result.setSamplerData(samplerData);
  }

  public SampleResult getResult() {
    return result;
  }

  public void recordStart() {
    result.sampleStart();
  }

  protected void setEndTime() {
    if (result.getEndTime() == 0) {
      result.sampleEnd();
    }
  }

  public void recordSuccess(Object resp) {
    this.setEndTime();
    String response = GrpcUtils.getJsonMessageResponse(resp);
    result.setSuccessful(true);
    result.setResponseCodeOK();
    result.setResponseData(response.getBytes());
    result.setResponseMessage(GrpcUtils.OK);
    result.setResponseCode(GrpcUtils.CODE_200);
  }

  public void recordFailure(Exception e) {
    this.setEndTime();
    result.setSuccessful(false);

    if (e.getCause() instanceof StatusRuntimeException) {
      Status status = ((StatusRuntimeException) e.getCause()).getStatus();
      int indexCode = status.getCode().value();
      String code = GrpcUtils.getStatusCode(indexCode);
      result.setResponseData(code.getBytes());
      result.setResponseCode(GrpcUtils.getStatusCodeNumber(code));
      result.setResponseMessage(code);
      return;
    }

    result.setResponseData(GrpcUtils.INVALID_ARGUMENT.getBytes());
    result.setResponseCode(GrpcUtils.CODE_400);
    result.setResponseMessage(e.toString());
  }
}
