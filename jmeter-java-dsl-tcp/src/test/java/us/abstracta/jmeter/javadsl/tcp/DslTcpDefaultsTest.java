package us.abstracta.jmeter.javadsl.tcp;

import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.tcp.TcpJmeterDsl.tcpDefaults;
import static us.abstracta.jmeter.javadsl.tcp.TcpJmeterDsl.tcpSampler;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.tcp.DslTcpSampler.TcpClientImpl;

public class DslTcpDefaultsTest {

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    protected CodeBuilderTest() {
      codeGenerator.addBuildersFrom(TcpJmeterDsl.class);
    }

    public DslTestPlan testPlanWithTcpDefaults() {
      return testPlan(
          tcpDefaults()
              .host("localhost")
              .port(8080)
              .eolByte(10),
          threadGroup(1, 1,
              tcpSampler()
                  .request("hello")
          )
      );
    }

    public DslTestPlan testPlanWithTcpDefaultsAndAllOptions() {
      return testPlan(
          tcpDefaults()
              .host("${HOST}")
              .port("${PORT}")
              .clientImpl(TcpClientImpl.LENGTH_PREFIXED_BINARY)
              .reuseConnection(false)
              .closeConnection(true)
              .noDelay(true)
              .connectionTimeout(Duration.ofSeconds(2))
              .responseTimeout(Duration.ofSeconds(5))
              .eolByte(10)
              .soLinger(Duration.ofSeconds(1)),
          threadGroup(1, 1,
              tcpSampler()
                  .request("0a0b")
          )
      );
    }

  }

}
