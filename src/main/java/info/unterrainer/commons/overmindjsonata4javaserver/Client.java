package info.unterrainer.commons.overmindjsonata4javaserver;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Client {

	public static void main(final String[] args) throws Exception {
		try (ZContext context = new ZContext()) {
			// Socket to talk to server
			ZMQ.Socket socket = context.createSocket(SocketType.REQ);
			socket.connect("tcp://10.10.196.2:5555");

			ZMsg msg = new ZMsg();
			msg.add("This is a test.");
			msg.send(socket);
			msg.destroy();

			// Block until a message is received
			ZMsg reply = ZMsg.recvMsg(socket);
			System.out.println("Received: [" + readAll(reply) + "]");
			reply.destroy();
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
