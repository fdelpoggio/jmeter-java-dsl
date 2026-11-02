package us.abstracta.jmeter.javadsl.tcp;

import java.time.Duration;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.BoolParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.tcp.DslTcpSampler.TcpClientImpl;

/**
 * Contains common logic used by TCP samplers and config elements.
 */
/*
Implemented as a collection of static methods (instead of a common super class) since TCP sampler
and TCP defaults need to extend different base classes (BaseSampler and BaseConfigElement).
*/
class TcpElementHelper {

  private TcpElementHelper() {
  }

  static void modifyTestElementServer(TestElement elem, String host, String port) {
    setStringProperty(elem, TCPSampler.SERVER, host);
    setStringProperty(elem, TCPSampler.PORT, port);
  }

  static void setStringProperty(TestElement elem, String propName, String value) {
    if (value != null) {
      elem.setProperty(propName, value);
    }
  }

  static void modifyTestElementConnection(TestElement elem, String clientImpl,
      Boolean reuseConnection, Boolean closeConnection, Boolean noDelay,
      Duration connectionTimeout, Duration responseTimeout, Integer eolByte, Duration soLinger) {
    setStringProperty(elem, TCPSampler.CLASSNAME, clientImpl);
    setBooleanProperty(elem, TCPSampler.RE_USE_CONNECTION, reuseConnection);
    setBooleanProperty(elem, TCPSampler.CLOSE_CONNECTION, closeConnection);
    setBooleanProperty(elem, TCPSampler.NODELAY, noDelay);
    if (connectionTimeout != null) {
      elem.setProperty(TCPSampler.TIMEOUT_CONNECT, String.valueOf(connectionTimeout.toMillis()));
    }
    if (responseTimeout != null) {
      elem.setProperty(TCPSampler.TIMEOUT, String.valueOf(responseTimeout.toMillis()));
    }
    if (eolByte != null) {
      elem.setProperty(TCPSampler.EOL_BYTE, String.valueOf(eolByte));
    }
    if (soLinger != null) {
      elem.setProperty(TCPSampler.SO_LINGER, String.valueOf(soLinger.getSeconds()));
    }
  }

  private static void setBooleanProperty(TestElement elem, String propName, Boolean value) {
    if (value != null) {
      elem.setProperty(propName, value);
    }
  }

  static int validateEolByte(int eolByte) {
    if (eolByte < Byte.MIN_VALUE || eolByte > Byte.MAX_VALUE) {
      throw new IllegalArgumentException(
          "EOL byte must be between " + Byte.MIN_VALUE + " and " + Byte.MAX_VALUE + ", but was "
              + eolByte);
    }
    return eolByte;
  }

  static void chainServerToMethodCall(MethodCall ret, TestElementParamBuilder paramBuilder) {
    ret.chain("host", paramBuilder.stringParam(TCPSampler.SERVER))
        .chain("port", paramBuilder.intParam(TCPSampler.PORT));
  }

  static void chainConnectionToMethodCall(MethodCall ret, TestElementParamBuilder paramBuilder) {
    ret.chain("clientImpl", buildClientImplParam(paramBuilder))
        .chain("reuseConnection", buildOptionalBoolParam(paramBuilder,
            TCPSampler.RE_USE_CONNECTION, TCPSampler.RE_USE_CONNECTION_DEFAULT))
        .chain("closeConnection", buildOptionalBoolParam(paramBuilder,
            TCPSampler.CLOSE_CONNECTION, TCPSampler.CLOSE_CONNECTION_DEFAULT))
        .chain("noDelay", buildOptionalBoolParam(paramBuilder, TCPSampler.NODELAY, false))
        .chain("connectionTimeout",
            paramBuilder.durationParamMillis(TCPSampler.TIMEOUT_CONNECT, null))
        .chain("responseTimeout", paramBuilder.durationParamMillis(TCPSampler.TIMEOUT, null))
        .chain("eolByte", paramBuilder.intParam(TCPSampler.EOL_BYTE))
        .chain("soLinger", paramBuilder.durationParam(TCPSampler.SO_LINGER));
  }

  private static MethodParam buildClientImplParam(TestElementParamBuilder paramBuilder) {
    String className = paramBuilder.prop(TCPSampler.CLASSNAME).getStringValue();
    TcpClientImpl clientImpl = TcpClientImpl.fromClassName(className);
    return clientImpl != null
        ? new EnumParam<>(TcpClientImpl.class, clientImpl.propertyValue(), null)
        : new StringParam(className);
  }

  /*
   JMeter GUI always sets boolean properties, but the DSL only sets them when explicitly specified,
   so a missing property has to be considered as the default value and not as false.
   */
  private static MethodParam buildOptionalBoolParam(TestElementParamBuilder paramBuilder,
      String propName, boolean defaultValue) {
    return paramBuilder.prop(propName).getStringValue().isEmpty()
        ? new BoolParam(defaultValue, defaultValue)
        : paramBuilder.boolParam(propName, defaultValue);
  }

}
