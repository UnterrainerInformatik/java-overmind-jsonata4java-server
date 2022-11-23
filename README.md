# java-overmind-jsonata4java-server
A very specific server, that connects to the database and allows the caller to do jsonata queries on the json files in the database by calling the server via socket-messaging (ZeroMQ).

## Usage
Put it in a `docker-compose.yml` file and map the following ports:

`5555` ... this is the port for the socket-server.
'50051'... this is the port for the gRPC server.