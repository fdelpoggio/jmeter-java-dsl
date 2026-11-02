### TCP

When you need to test services that use a custom protocol over raw TCP connections (like legacy systems, financial protocols, IoT gateways, etc.), you can use the DSL provided TCP elements, which are based on [JMeter TCP Sampler](https://jmeter.apache.org/usermanual/component_reference.html#TCP_Sampler).

To use them, add the following dependency to your project:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-tcp</artifactId>
  <version>2.3</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-tcp:2.3'
```
:::
::::

And use them like in this example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.tcp.TcpJmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            tcpSampler("my.service", 9000)
                .request("PING\n")
                .eolByte('\n')
                .connectionTimeout(Duration.ofSeconds(2))
                .responseTimeout(Duration.ofSeconds(5))
                .children(
                    responseAssertion().containsSubstrings("PONG")
                )
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

By default, the sampler sends and receives texts, reuses the connection between samples of the same thread, and reads the response until the server closes the connection or the response timeout is reached.

::: tip
Always set `eolByte` when the server keeps the connection open after responding, and a response timeout to quickly identify unexpected behaviors. Otherwise, the sampler may wait indefinitely for the response to end.
:::

To send binary data you can use `request(byte[])`, which automatically uses the binary client and encodes the data as hex string. In such case, responses are also returned as hex strings:

```java
tcpSampler("my.service", 9000)
    .request(new byte[]{0x01, 0x02, (byte) 0xff})
```

If your protocol prefixes messages with their length, then use `TcpClientImpl.LENGTH_PREFIXED_BINARY`:

```java
tcpSampler("my.service", 9000)
    .clientImpl(TcpClientImpl.LENGTH_PREFIXED_BINARY)
    .request(new byte[]{0x01, 0x02, 0x03})
```

::: tip
The length of the prefix (2 bytes by default) can be changed with `tcp.binarylength.prefix.length` JMeter property. Check [JMeter TCP Sampler documentation](https://jmeter.apache.org/usermanual/component_reference.html#TCP_Sampler) for additional JMeter properties that affect TCP samplers, like `tcp.charset` which allows changing the charset used by the text client.
:::

You can use `reuseConnection(false)` or `closeConnection(true)` to create a new connection for each sample, `noDelay(true)` to disable Nagle's algorithm, `soLinger` to set linger timeout when closing connections, and `clientImpl(String)` to use a custom TCP client implementation.

Additionally, you can use `tcpDefaults` to avoid repeating the same settings in several TCP samplers:

```java
testPlan(
    tcpDefaults()
        .host("my.service")
        .port(9000)
        .eolByte('\n')
        .responseTimeout(Duration.ofSeconds(5)),
    threadGroup(2, 10,
        tcpSampler("login")
            .request("LOGIN ${USER}\n"),
        tcpSampler("logout")
            .request("LOGOUT\n")
    )
)
```

Check [TcpJmeterDsl](/jmeter-java-dsl-tcp/src/main/java/us/abstracta/jmeter/javadsl/tcp/TcpJmeterDsl.java) for additional details and options and [TcpJmeterDslTest](/jmeter-java-dsl-tcp/src/test/java/us/abstracta/jmeter/javadsl/tcp/TcpJmeterDslTest.java) for additional examples.
