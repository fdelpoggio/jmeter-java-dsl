package us.abstracta.jmeter.javadsl.tcp;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.tcp.config.gui.TCPConfigGui;
import org.apache.jmeter.protocol.tcp.sampler.TCPClient;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleGuiClassCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.core.configs.BaseConfigElement;
import us.abstracta.jmeter.javadsl.tcp.DslTcpSampler.TcpClientImpl;

/**
 * Allows configuring default values for common properties of TCP samplers.
 * <p>
 * Values set in a TCP sampler take precedence over the ones defined in this element.
 * <p>
 * This is mainly a way to avoid duplication and an alternative to using java variables or builder
 * methods. When in doubt, prefer using java variables or custom defined builder methods since they
 * are easier to write (in some cases), read and identify their scope.
 *
 * @see DslTcpSampler
 * @since 2.4
 */
public class DslTcpDefaults extends BaseConfigElement {

  protected String host;
  protected String port;
  protected String clientImpl;
  protected Boolean reuseConnection;
  protected Boolean closeConnection;
  protected Boolean noDelay;
  protected Duration connectionTimeout;
  protected Duration responseTimeout;
  protected Integer eolByte;
  protected Duration soLinger;

  public DslTcpDefaults() {
    super("TCP Sampler Config", TCPConfigGui.class);
  }

  /**
   * Specifies the default server host (name or IP address) to be used by TCP samplers.
   *
   * @param host specifies the server host name or IP address. It may contain JMeter expressions.
   * @return the config element for further configuration or usage.
   */
  public DslTcpDefaults host(String host) {
    this.host = host;
    return this;
  }

  /**
   * Specifies the default server port to be used by TCP samplers.
   *
   * @param port specifies the server port number.
   * @return the config element for further configuration or usage.
   */
  public DslTcpDefaults port(int port) {
    this.port = String.valueOf(port);
    return this;
  }

  /**
   * Same as {@link #port(int)} but allowing to use JMeter expressions (variables or functions) to
   * solve the actual port value.
   *
   * @param port a JMeter expression that returns the server port number.
   * @return the config element for further configuration or usage.
   * @see #port(int)
   */
  public DslTcpDefaults port(String port) {
    this.port = port;
    return this;
  }

  /**
   * Specifies the default TCP client implementation to be used by TCP samplers.
   *
   * @param clientImpl specifies the client implementation to use.
   * @return the config element for further configuration or usage.
   * @see DslTcpSampler#clientImpl(TcpClientImpl)
   */
  public DslTcpDefaults clientImpl(TcpClientImpl clientImpl) {
    this.clientImpl = clientImpl.propertyValue();
    return this;
  }

  /**
   * Same as {@link #clientImpl(TcpClientImpl)} but allowing to specify a custom implementation
   * class name or a JMeter expression which solves to the implementation class name.
   *
   * @param className specifies the fully qualified class name of the {@link TCPClient}
   *                  implementation to use.
   * @return the config element for further configuration or usage.
   * @see DslTcpSampler#clientImpl(String)
   */
  public DslTcpDefaults clientImpl(String className) {
    this.clientImpl = className;
    return this;
  }

  /**
   * Specifies whether TCP samplers should reuse the connection between samples of the same thread
   * by default.
   *
   * @param reuseConnection specifies to reuse connections when true. By default, connections are
   *                        reused.
   * @return the config element for further configuration or usage.
   * @see DslTcpSampler#reuseConnection(boolean)
   */
  public DslTcpDefaults reuseConnection(boolean reuseConnection) {
    this.reuseConnection = reuseConnection;
    return this;
  }

  /**
   * Specifies whether TCP samplers should close the connection after each sample by default.
   *
   * @param closeConnection specifies to close connections after each sample when true. By default,
   *                        connections are not closed.
   * @return the config element for further configuration or usage.
   * @see DslTcpSampler#closeConnection(boolean)
   */
  public DslTcpDefaults closeConnection(boolean closeConnection) {
    this.closeConnection = closeConnection;
    return this;
  }

  /**
   * Specifies whether TCP samplers should disable Nagle's algorithm (TCP_NODELAY) by default.
   *
   * @param noDelay specifies to disable Nagle's algorithm when true. By default, it is false.
   * @return the config element for further configuration or usage.
   * @see DslTcpSampler#noDelay(boolean)
   */
  public DslTcpDefaults noDelay(boolean noDelay) {
    this.noDelay = noDelay;
    return this;
  }

  /**
   * Allows setting the default maximum amount of time to wait for a connection to be established.
   *
   * @param timeout specifies the duration to be used as connection timeout. When set to 0 no
   *                timeout is used (wait indefinitely), which is not recommended.
   * @return the config element for further configuration or usage.
   * @see DslTcpSampler#connectionTimeout(Duration)
   */
  public DslTcpDefaults connectionTimeout(Duration timeout) {
    this.connectionTimeout = timeout;
    return this;
  }

  /**
   * Allows setting the default maximum amount of time to wait for a response.
   *
   * @param timeout specifies the duration to be used as response timeout. When set to 0 no timeout
   *                is used (wait indefinitely), which is not recommended.
   * @return the config element for further configuration or usage.
   * @see DslTcpSampler#responseTimeout(Duration)
   */
  public DslTcpDefaults responseTimeout(Duration timeout) {
    this.responseTimeout = timeout;
    return this;
  }

  /**
   * Specifies the default byte value which marks the end of a response.
   *
   * @param eolByte specifies the byte value marking the end of the response. Has to be between
   *                -128 and 127. E.g.: '\n'.
   * @return the config element for further configuration or usage.
   * @throws IllegalArgumentException when the value is out of byte range.
   * @see DslTcpSampler#eolByte(int)
   */
  public DslTcpDefaults eolByte(int eolByte) {
    this.eolByte = TcpElementHelper.validateEolByte(eolByte);
    return this;
  }

  /**
   * Enables SO_LINGER by default on connections with the given timeout.
   *
   * @param soLinger specifies the linger timeout. Since sockets only support seconds, any smaller
   *                 granularity is truncated.
   * @return the config element for further configuration or usage.
   * @see DslTcpSampler#soLinger(Duration)
   */
  public DslTcpDefaults soLinger(Duration soLinger) {
    this.soLinger = soLinger;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    ConfigTestElement ret = new ConfigTestElement();
    TcpElementHelper.modifyTestElementServer(ret, host, port);
    TcpElementHelper.modifyTestElementConnection(ret, clientImpl, reuseConnection,
        closeConnection, noDelay, connectionTimeout, responseTimeout, eolByte, soLinger);
    return ret;
  }

  public static class CodeBuilder extends SingleGuiClassCallBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(TCPConfigGui.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(MethodCallContext context) {
      MethodCall ret = buildMethodCall();
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(context.getTestElement());
      TcpElementHelper.chainServerToMethodCall(ret, paramBuilder);
      TcpElementHelper.chainConnectionToMethodCall(ret, paramBuilder);
      return ret;
    }

  }

}
