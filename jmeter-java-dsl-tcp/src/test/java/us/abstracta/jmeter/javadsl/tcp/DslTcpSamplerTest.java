package us.abstracta.jmeter.javadsl.tcp;

import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.tcp.TcpJmeterDsl.tcpSampler;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.tcp.DslTcpSampler.TcpClientImpl;

public class DslTcpSamplerTest {

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    protected CodeBuilderTest() {
      codeGenerator.addBuildersFrom(TcpJmeterDsl.class);
    }

    public DslTestPlan testPlanWithTcpSampler() {
      return testPlan(
          threadGroup(1, 1,
              tcpSampler("localhost", 8080)
                  .request("hello")
          )
      );
    }

    public DslTestPlan testPlanWithTcpSamplerAndAllOptions() {
      return testPlan(
          threadGroup(1, 1,
              tcpSampler("mySampler", "localhost", 8080)
                  .request("0a0b")
                  .clientImpl(TcpClientImpl.BINARY)
                  .reuseConnection(false)
                  .closeConnection(true)
                  .noDelay(true)
                  .connectionTimeout(Duration.ofSeconds(2))
                  .responseTimeout(Duration.ofSeconds(5))
                  .eolByte(10)
                  .soLinger(Duration.ofSeconds(1))
                  .credentials("user", "pass")
          )
      );
    }

    public DslTestPlan testPlanWithTcpSamplerAndJmeterExpressions() {
      return testPlan(
          threadGroup(1, 1,
              tcpSampler("${HOST}", "${PORT}")
                  .request("${REQUEST}")
          )
      );
    }

    public DslTestPlan testPlanWithTcpSamplerWithoutServer() {
      return testPlan(
          threadGroup(1, 1,
              tcpSampler()
                  .port(8080)
                  .request("hello")
          )
      );
    }

    public DslTestPlan testPlanWithNamedTcpSamplerWithoutServer() {
      return testPlan(
          threadGroup(1, 1,
              tcpSampler("mySampler")
                  .request("hello")
          )
      );
    }

    public DslTestPlan testPlanWithTcpSamplerAndCustomClientImpl() {
      return testPlan(
          threadGroup(1, 1,
              tcpSampler("localhost", 8080)
                  .request("hello")
                  .clientImpl("com.example.MyTcpClient")
          )
      );
    }

  }

}
