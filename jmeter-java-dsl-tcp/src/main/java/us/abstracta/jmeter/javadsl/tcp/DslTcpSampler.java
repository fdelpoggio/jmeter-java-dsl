package us.abstracta.jmeter.javadsl.tcp;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.tcp.control.gui.TCPSamplerGui;
import org.apache.jmeter.protocol.tcp.sampler.BinaryTCPClientImpl;
import org.apache.jmeter.protocol.tcp.sampler.LengthPrefixedBinaryTCPClientImpl;
import org.apache.jmeter.protocol.tcp.sampler.TCPClient;
import org.apache.jmeter.protocol.tcp.sampler.TCPClientImpl;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.util.JOrphanUtils;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam.EnumPropertyValue;
import us.abstracta.jmeter.javadsl.core.samplers.BaseSampler;

/**
 * Allows sending requests to, and reading responses from, a server through a raw TCP connection.
 * <p>
 * By default, the sampler uses a text client ({@link TcpClientImpl#TEXT}), reuses the connection
 * between samples of the same thread, and reads the response until the server closes the
 * connection or the response timeout is reached. Check {@link #eolByte(int)} to specify the byte
 * which marks the end of a response, and {@link #responseTimeout(Duration)} to avoid waiting
 * indefinitely for a response.
 *
 * @see DslTcpDefaults
 * @since 2.4
 */
public class DslTcpSampler extends BaseSampler<DslTcpSampler> {

  private static final String DEFAULT_NAME = "TCP Sampler";

  protected String host;
  protected String port;
  protected String request;
  protected String clientImpl;
  protected Boolean reuseConnection;
  protected Boolean closeConnection;
  protected Boolean noDelay;
  protected Duration connectionTimeout;
  protected Duration responseTimeout;
  protected Integer eolByte;
  protected Duration soLinger;
  protected String username;
  protected String password;

  public DslTcpSampler(String name, String host, String port) {
    super(name != null ? name : DEFAULT_NAME, TCPSamplerGui.class);
    this.host = host;
    this.port = port;
  }

  /**
   * Specifies the TCP client implementation to use for sending requests and reading responses.
   */
  public enum TcpClientImpl implements EnumPropertyValue {
    /**
     * Sends the request as text and returns the response as text.
     * <p>
     * The charset used to encode and decode texts can be configured with tcp.charset JMeter
     * property, and defaults to the platform default charset.
     */
    TEXT(TCPClientImpl.class),
    /**
     * Sends the request as binary data, requiring the request to be specified as a hex encoded
     * string (or with {@link DslTcpSampler#request(byte[])}), and returns the response as a hex
     * encoded string.
     */
    BINARY(BinaryTCPClientImpl.class),
    /**
     * Same as {@link #BINARY}, but prefixing the request with its length, and reading the response
     * length from the response prefix.
     * <p>
     * The length of the prefix (in bytes) can be configured with tcp.binarylength.prefix.length
     * JMeter property, and defaults to 2 bytes.
     */
    LENGTH_PREFIXED_BINARY(LengthPrefixedBinaryTCPClientImpl.class);

    private final Class<? extends TCPClient> clientClass;

    TcpClientImpl(Class<? extends TCPClient> clientClass) {
      this.clientClass = clientClass;
    }

    @Override
    public String propertyValue() {
      return clientClass.getSimpleName();
    }

    static TcpClientImpl fromClassName(String className) {
      return Arrays.stream(values())
          .filter(v -> v.clientClass.getSimpleName().equals(className)
              || v.clientClass.getName().equals(className))
          .findAny()
          .orElse(null);
    }

  }

  /**
   * Specifies the server host (name or IP address) to connect to.
   * <p>
   * This is useful when the sampler was created without host (eg: to use the host specified in
   * {@link DslTcpDefaults}), but some particular sampler requires a different one.
   *
   * @param host specifies the server host name or IP address. It may contain JMeter expressions.
   * @return the sampler for further configuration or usage.
   */
  public DslTcpSampler host(String host) {
    this.host = host;
    return this;
  }

  /**
   * Specifies the server port to connect to.
   *
   * @param port specifies the server port number.
   * @return the sampler for further configuration or usage.
   * @see #host(String)
   */
  public DslTcpSampler port(int port) {
    this.port = String.valueOf(port);
    return this;
  }

  /**
   * Same as {@link #port(int)} but allowing to use JMeter expressions (variables or functions) to
   * solve the actual port value.
   *
   * @param port a JMeter expression that returns the server port number.
   * @return the sampler for further configuration or usage.
   * @see #port(int)
   */
  public DslTcpSampler port(String port) {
    this.port = port;
    return this;
  }

  /**
   * Specifies the data to send to the server.
   * <p>
   * When using {@link TcpClientImpl#BINARY} or {@link TcpClientImpl#LENGTH_PREFIXED_BINARY}, the
   * request has to be a hex encoded string (eg: "0a0b0c"). Consider using {@link #request(byte[])}
   * in such scenarios.
   *
   * @param request specifies the data to send to the server. It may contain JMeter expressions.
   * @return the sampler for further configuration or usage.
   */
  public DslTcpSampler request(String request) {
    this.request = request;
    return this;
  }

  /**
   * Same as {@link #request(String)} but allowing to specify binary data to send to the server.
   * <p>
   * The data is converted to a hex encoded string, as required by {@link TcpClientImpl#BINARY} and
   * {@link TcpClientImpl#LENGTH_PREFIXED_BINARY}. If no client implementation has been set, then
   * {@link TcpClientImpl#BINARY} is used.
   *
   * @param request specifies the binary data to send to the server.
   * @return the sampler for further configuration or usage.
   */
  public DslTcpSampler request(byte[] request) {
    this.request = JOrphanUtils.baToHexString(request);
    if (clientImpl == null) {
      clientImpl = TcpClientImpl.BINARY.propertyValue();
    }
    return this;
  }

  /**
   * Specifies the TCP client implementation to use for sending requests and reading responses.
   *
   * @param clientImpl specifies the client implementation to use. If none is set, then
   *                   {@link TcpClientImpl#TEXT} is used, unless tcp.handler JMeter property is
   *                   set to another implementation.
   * @return the sampler for further configuration or usage.
   * @see TcpClientImpl
   */
  public DslTcpSampler clientImpl(TcpClientImpl clientImpl) {
    this.clientImpl = clientImpl.propertyValue();
    return this;
  }

  /**
   * Same as {@link #clientImpl(TcpClientImpl)} but allowing to specify a custom implementation
   * class name or a JMeter expression which solves to the implementation class name.
   * <p>
   * This is useful when you have implemented a custom {@link TCPClient} and included it in the
   * classpath.
   *
   * @param className specifies the fully qualified class name of the {@link TCPClient}
   *                  implementation to use.
   * @return the sampler for further configuration or usage.
   * @see #clientImpl(TcpClientImpl)
   */
  public DslTcpSampler clientImpl(String className) {
    this.clientImpl = className;
    return this;
  }

  /**
   * Specifies whether to reuse the connection between samples of the same thread.
   *
   * @param reuseConnection specifies to reuse the connection when true, or to create a new
   *                        connection for each sample when false. By default, connections are
   *                        reused.
   * @return the sampler for further configuration or usage.
   */
  public DslTcpSampler reuseConnection(boolean reuseConnection) {
    this.reuseConnection = reuseConnection;
    return this;
  }

  /**
   * Specifies whether to close the connection after each sample.
   *
   * @param closeConnection specifies to close the connection after the sample when true. By
   *                        default, connections are not closed.
   * @return the sampler for further configuration or usage.
   */
  public DslTcpSampler closeConnection(boolean closeConnection) {
    this.closeConnection = closeConnection;
    return this;
  }

  /**
   * Specifies whether to disable Nagle's algorithm (TCP_NODELAY) on the connection.
   *
   * @param noDelay specifies to disable Nagle's algorithm when true. By default, it is false.
   * @return the sampler for further configuration or usage.
   */
  public DslTcpSampler noDelay(boolean noDelay) {
    this.noDelay = noDelay;
    return this;
  }

  /**
   * Allows setting the maximum amount of time to wait for a connection to be established.
   *
   * @param timeout specifies the duration to be used as connection timeout. When set to 0, or when
   *                not set, no timeout is used (wait indefinitely), which is not recommended.
   * @return the sampler for further configuration or usage.
   */
  public DslTcpSampler connectionTimeout(Duration timeout) {
    this.connectionTimeout = timeout;
    return this;
  }

  /**
   * Allows setting the maximum amount of time to wait for a response.
   * <p>
   * This is particularly important when no {@link #eolByte(int)} is set and the server does not
   * close the connection after responding, since otherwise the sampler would wait indefinitely.
   *
   * @param timeout specifies the duration to be used as response timeout. When set to 0, or when
   *                not set, no timeout is used (wait indefinitely), which is not recommended.
   * @return the sampler for further configuration or usage.
   */
  public DslTcpSampler responseTimeout(Duration timeout) {
    this.responseTimeout = timeout;
    return this;
  }

  /**
   * Specifies the byte value which marks the end of a response.
   * <p>
   * When the sampler reads this byte at the end of the received data, it stops reading and
   * considers the response complete. This is useful when the server keeps the connection open
   * after responding.
   *
   * @param eolByte specifies the byte value marking the end of the response. Has to be between
   *                -128 and 127. E.g.: '\n'.
   * @return the sampler for further configuration or usage.
   * @throws IllegalArgumentException when the value is out of byte range.
   */
  public DslTcpSampler eolByte(int eolByte) {
    this.eolByte = TcpElementHelper.validateEolByte(eolByte);
    return this;
  }

  /**
   * Enables SO_LINGER on the connection with the given timeout.
   * <p>
   * This specifies how much time to wait, while closing the connection, for unsent data to be sent
   * to the server.
   *
   * @param soLinger specifies the linger timeout. Since sockets only support seconds, any smaller
   *                 granularity is truncated.
   * @return the sampler for further configuration or usage.
   */
  public DslTcpSampler soLinger(Duration soLinger) {
    this.soLinger = soLinger;
    return this;
  }

  /**
   * Specifies credentials to be used by the TCP client implementation.
   * <p>
   * Default JMeter TCP client implementations don't use these credentials, but they are available
   * to custom {@link TCPClient} implementations.
   *
   * @param username specifies the username.
   * @param password specifies the password.
   * @return the sampler for further configuration or usage.
   */
  public DslTcpSampler credentials(String username, String password) {
    this.username = username;
    this.password = password;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    TCPSampler ret = new TCPSampler();
    TcpElementHelper.modifyTestElementServer(ret, host, port);
    TcpElementHelper.setStringProperty(ret, TCPSampler.REQUEST, request);
    TcpElementHelper.modifyTestElementConnection(ret, clientImpl, reuseConnection,
        closeConnection, noDelay, connectionTimeout, responseTimeout, eolByte, soLinger);
    TcpElementHelper.setStringProperty(ret, ConfigTestElement.USERNAME, username);
    TcpElementHelper.setStringProperty(ret, ConfigTestElement.PASSWORD, password);
    return ret;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<TCPSampler> {

    public CodeBuilder(List<Method> builderMethods) {
      super(TCPSampler.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(TCPSampler testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      MethodParam name = paramBuilder.nameParam(DEFAULT_NAME);
      MethodParam host = paramBuilder.stringParam(TCPSampler.SERVER);
      MethodParam port = paramBuilder.intParam(TCPSampler.PORT);
      MethodCall ret;
      if (!host.isDefault() && !port.isDefault()) {
        ret = buildMethodCall(name, host, port);
      } else {
        ret = buildMethodCall(name);
        TcpElementHelper.chainServerToMethodCall(ret, paramBuilder);
      }
      ret.chain("request", paramBuilder.stringParam(TCPSampler.REQUEST));
      TcpElementHelper.chainConnectionToMethodCall(ret, paramBuilder);
      return ret.chain("credentials", paramBuilder.stringParam(ConfigTestElement.USERNAME),
          paramBuilder.stringParam(ConfigTestElement.PASSWORD));
    }

  }

}
