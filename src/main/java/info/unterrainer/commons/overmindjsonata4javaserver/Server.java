package info.unterrainer.commons.overmindjsonata4javaserver;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

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
				ZMsg msg = ZMsg.recvMsg(socket);
				if (msg != null) {
					String value = readAll(msg);
					msg.destroy();

					System.out.println("Received: [" + value + "]");
					// Send a response
					ZMsg reply = new ZMsg();
					reply.add("Hello. Your message was: \"");
					reply.add(value);
					reply.add("\"");
					reply.send(socket);
					reply.destroy();
				}
			}
		}
	}

	public static String readAll(final ZMsg msg) {
		if (msg == null)
			return null;
		StringBuilder sb = new StringBuilder();
		while (msg.peek() != null)
			sb.append(msg.popString());
		return sb.toString();
	}
}
