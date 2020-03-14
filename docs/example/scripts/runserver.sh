#!/bin/sh

cd server
rm -rf helloworld *.proto
mkdir helloworld && cp ../*.proto .
protoc --go_out=plugins=grpc:./helloworld *.proto
go run ./hello_server.go