package us.abstracta.jmeter.javadsl.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TcpEchoServer implements AutoCloseable {

  private final ServerSocket serverSocket;
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final AtomicInteger connectionsCount = new AtomicInteger();

  public TcpEchoServer() throws IOException {
    serverSocket = new ServerSocket(0);
    executor.submit(this::acceptConnections);
  }

  private void acceptConnections() {
    while (!serverSocket.isClosed()) {
      try {
        Socket socket = serverSocket.accept();
        connectionsCount.incrementAndGet();
        executor.submit(() -> echo(socket));
      } catch (IOException e) {
        // server socket closed
      }
    }
  }

  private void echo(Socket socket) {
    try (Socket s = socket) {
      InputStream input = s.getInputStream();
      OutputStream output = s.getOutputStream();
      byte[] buffer = new byte[4096];
      int read;
      while ((read = input.read(buffer)) != -1) {
        output.write(buffer, 0, read);
        output.flush();
      }
    } catch (IOException e) {
      // connection closed by client
    }
  }

  public int getPort() {
    return serverSocket.getLocalPort();
  }

  public int getConnectionsCount() {
    return connectionsCount.get();
  }

  @Override
  public void close() throws IOException {
    serverSocket.close();
    executor.shutdownNow();
  }

}
