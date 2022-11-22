package info.unterrainer.commons.overmindjsonata4javaserver.implementations;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import info.unterrainer.commons.jreutils.ShutdownHook;
import info.unterrainer.commons.overmindjsonata4javaserver.implementations.protos.GreeterGrpc;
import info.unterrainer.commons.overmindjsonata4javaserver.implementations.protos.HelloReply;
import info.unterrainer.commons.overmindjsonata4javaserver.implementations.protos.HelloRequest;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GRpcServer {

	private Server server;

	public void run() throws IOException {

		int port = 50051;
		server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create()).addService(new GreeterImpl())
				.build().start();
		log.info("Server started, listening on port [{}]", port);

		ShutdownHook.register(() -> {
			try {
				GRpcServer.this.stop();
			} catch (InterruptedException e) {
				log.error("Exception shutting down gRPC server.", e);
			}
			System.err.println("*** server shut down");
		});

		try {

		} catch (Exception e) {
			log.error("uncaught exception", e);
		}
	}

	public void stop() throws InterruptedException {
		if (server != null) {
			server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon
	 * threads.
	 */
	public void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

		@Override
		public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
			HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
	}
}
