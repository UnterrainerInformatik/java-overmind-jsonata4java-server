package info.unterrainer.commons.overmindjsonata4javaserver.implementations;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import info.unterrainer.commons.jreutils.ShutdownHook;
import info.unterrainer.commons.overmindjsonata4javaserver.implementations.protos.JsonataGrpc;
import info.unterrainer.commons.overmindjsonata4javaserver.implementations.protos.QueryData;
import info.unterrainer.commons.overmindjsonata4javaserver.implementations.protos.QueryResult;
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
		server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create()).addService(new JsonataImpl())
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

	static class JsonataImpl extends JsonataGrpc.JsonataImplBase {

		@Override
		public void evaluate(QueryData req, StreamObserver<QueryResult> responseObserver) {
			String result = info.unterrainer.commons.overmindjsonata4javaserver.Server.jsonataQuery(req.getQuery(), req.getJson());
			QueryResult reply = QueryResult.newBuilder().setValue(result == null ? false : result.equalsIgnoreCase("true")).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
	}
}
