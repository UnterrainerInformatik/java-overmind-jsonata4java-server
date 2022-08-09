package info.unterrainer.commons.overmindjsonata4javaserver;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Server {

	public static void main(final String[] args) throws Exception {
		try (ZContext context = new ZContext()) {
			// Socket to talk to clients
			ZMQ.Socket socket = context.createSocket(SocketType.REP);
			socket.bind("tcp://*:5555");

			while (!Thread.currentThread().isInterrupted()) {
				// Block until a message is received
				byte[] reply = socket.recv(0);

				// Print the message
				System.out.println("Received: [" + new String(reply, ZMQ.CHARSET) + "]");

				// Send a response
				String response = "Hello, world!";
				socket.send(response.getBytes(ZMQ.CHARSET), 0);
			}
		}
	}
}
