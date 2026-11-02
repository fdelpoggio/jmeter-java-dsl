package us.abstracta.jmeter.javadsl.tcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.abstracta.jmeter.javadsl.JmeterDsl.responseAssertion;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.tcp.TcpJmeterDsl.tcpDefaults;
import static us.abstracta.jmeter.javadsl.tcp.TcpJmeterDsl.tcpSampler;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.tcp.DslTcpSampler.TcpClientImpl;

public class TcpJmeterDslTest {

  private static final String HOST = "localhost";
  private static final String TEXT_REQUEST = "Hello TCP!\n";
  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private TcpEchoServer server;

  @BeforeEach
  public void setup() throws Exception {
    server = new TcpEchoServer();
  }

  @AfterEach
  public void tearDown() throws Exception {
    server.close();
  }

  @Test
  public void shouldGetEchoedTextWhenTcpSamplerWithTextRequestAndEolByte() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            tcpSampler(HOST, server.getPort())
                .request(TEXT_REQUEST)
                .eolByte('\n')
                .responseTimeout(TIMEOUT)
                .children(
                    responseAssertion().containsSubstrings(TEXT_REQUEST.trim())
                )
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isZero();
  }

  @Test
  public void shouldGetEchoedHexWhenTcpSamplerWithBinaryRequestAndEolByte() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            tcpSampler(HOST, server.getPort())
                .request(new byte[]{0x01, 0x02, (byte) 0xff, '\n'})
                .eolByte('\n')
                .responseTimeout(TIMEOUT)
                .children(
                    responseAssertion().containsSubstrings("0102ff0a")
                )
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isZero();
  }

  @Test
  public void shouldGetEchoedHexWhenTcpSamplerWithLengthPrefixedBinaryClient() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            tcpSampler(HOST, server.getPort())
                .clientImpl(TcpClientImpl.LENGTH_PREFIXED_BINARY)
                .request(new byte[]{0x01, 0x02, 0x03})
                .responseTimeout(TIMEOUT)
                .children(
                    responseAssertion().containsSubstrings("010203")
                )
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isZero();
  }

  @Test
  public void shouldUseSingleConnectionWhenTcpSamplersWithDefaultReuseConnection()
      throws Exception {
    testPlan(
        threadGroup(1, 3,
            buildEchoSampler()
        )
    ).run();
    assertThat(server.getConnectionsCount()).isEqualTo(1);
  }

  private DslTcpSampler buildEchoSampler() {
    return tcpSampler(HOST, server.getPort())
        .request(TEXT_REQUEST)
        .eolByte('\n')
        .responseTimeout(TIMEOUT);
  }

  @Test
  public void shouldUseConnectionPerSampleWhenTcpSamplersWithoutReuseConnection()
      throws Exception {
    testPlan(
        threadGroup(1, 3,
            buildEchoSampler()
                .reuseConnection(false)
        )
    ).run();
    assertThat(server.getConnectionsCount()).isEqualTo(3);
  }

  @Test
  public void shouldUseConnectionPerSampleWhenTcpSamplersWithCloseConnection() throws Exception {
    testPlan(
        threadGroup(1, 3,
            buildEchoSampler()
                .closeConnection(true)
        )
    ).run();
    assertThat(server.getConnectionsCount()).isEqualTo(3);
  }

  @Test
  public void shouldGetEchoedTextWhenTcpSamplerWithServerAndOptionsFromDefaults()
      throws Exception {
    TestPlanStats stats = testPlan(
        tcpDefaults()
            .host(HOST)
            .port(server.getPort())
            .eolByte('\n')
            .responseTimeout(TIMEOUT),
        threadGroup(1, 1,
            tcpSampler()
                .request(TEXT_REQUEST)
                .children(
                    responseAssertion().containsSubstrings(TEXT_REQUEST.trim())
                )
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isZero();
  }

  @Test
  public void shouldUseSamplerValuesWhenTcpSamplerOverridesDefaults() throws Exception {
    testPlan(
        tcpDefaults()
            .reuseConnection(false),
        threadGroup(1, 3,
            buildEchoSampler()
                .reuseConnection(true)
        )
    ).run();
    assertThat(server.getConnectionsCount()).isEqualTo(1);
  }

  @Test
  public void shouldGetErrorWhenTcpSamplerWithoutEolByteAndServerNotClosingConnection()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            tcpSampler(HOST, server.getPort())
                .request(TEXT_REQUEST)
                .responseTimeout(Duration.ofMillis(500))
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldGetErrorWhenTcpSamplerToUnavailableServer() throws Exception {
    int port = server.getPort();
    server.close();
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            tcpSampler(HOST, port)
                .request(TEXT_REQUEST)
                .connectionTimeout(TIMEOUT)
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldThrowIllegalArgumentExceptionWhenEolByteOutOfRange() {
    assertThrows(IllegalArgumentException.class, () -> tcpSampler(HOST, 80).eolByte(200));
  }

}
