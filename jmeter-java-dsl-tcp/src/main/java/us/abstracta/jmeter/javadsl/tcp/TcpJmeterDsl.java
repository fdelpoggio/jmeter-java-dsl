package us.abstracta.jmeter.javadsl.tcp;

/**
 * Includes builder methods for TCP test elements which allow interacting with servers through raw
 * TCP connections.
 *
 * @since 2.4
 */
public class TcpJmeterDsl {

  private TcpJmeterDsl() {
  }

  /**
   * Builds a TCP sampler which allows sending requests to, and reading responses from, a server
   * through a TCP connection.
   *
   * @param host specifies the server host name or IP address. It may contain JMeter expressions.
   * @param port specifies the server port number.
   * @return the sampler instance for further configuration or usage in test plan.
   * @see DslTcpSampler
   */
  public static DslTcpSampler tcpSampler(String host, int port) {
    return new DslTcpSampler(null, host, String.valueOf(port));
  }

  /**
   * Same as {@link #tcpSampler(String, int)} but allowing to use JMeter expressions (variables or
   * functions) to solve the actual port value.
   *
   * @param host specifies the server host name or IP address. It may contain JMeter expressions.
   * @param port a JMeter expression that returns the server port number.
   * @return the sampler instance for further configuration or usage in test plan.
   * @see #tcpSampler(String, int)
   */
  public static DslTcpSampler tcpSampler(String host, String port) {
    return new DslTcpSampler(null, host, port);
  }

  /**
   * Same as {@link #tcpSampler(String, int)} but allowing to set a name to the sampler for easy
   * identification in collected statistics and metrics.
   *
   * @param name specifies the name to assign to the sampler.
   * @param host specifies the server host name or IP address. It may contain JMeter expressions.
   * @param port specifies the server port number.
   * @return the sampler instance for further configuration or usage in test plan.
   * @see #tcpSampler(String, int)
   */
  public static DslTcpSampler tcpSampler(String name, String host, int port) {
    return new DslTcpSampler(name, host, String.valueOf(port));
  }

  /**
   * Same as {@link #tcpSampler(String, String, int)} but allowing to use JMeter expressions
   * (variables or functions) to solve the actual port value.
   *
   * @param name specifies the name to assign to the sampler.
   * @param host specifies the server host name or IP address. It may contain JMeter expressions.
   * @param port a JMeter expression that returns the server port number.
   * @return the sampler instance for further configuration or usage in test plan.
   * @see #tcpSampler(String, String, int)
   */
  public static DslTcpSampler tcpSampler(String name, String host, String port) {
    return new DslTcpSampler(name, host, port);
  }

  /**
   * Builds a TCP sampler with no host and port, which is useful when they are specified with
   * {@link #tcpDefaults()}.
   *
   * @return the sampler instance for further configuration or usage in test plan.
   * @see #tcpDefaults()
   */
  public static DslTcpSampler tcpSampler() {
    return new DslTcpSampler(null, null, null);
  }

  /**
   * Same as {@link #tcpSampler()} but allowing to set a name to the sampler for easy
   * identification in collected statistics and metrics.
   *
   * @param name specifies the name to assign to the sampler.
   * @return the sampler instance for further configuration or usage in test plan.
   * @see #tcpSampler()
   */
  public static DslTcpSampler tcpSampler(String name) {
    return new DslTcpSampler(name, null, null);
  }

  /**
   * Builds a config element which allows specifying default values for TCP samplers.
   *
   * @return the config element for further configuration or usage in test plan.
   * @see DslTcpDefaults
   */
  public static DslTcpDefaults tcpDefaults() {
    return new DslTcpDefaults();
  }

}
