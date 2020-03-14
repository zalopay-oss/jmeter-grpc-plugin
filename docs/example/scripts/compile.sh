#!/bin/sh

PROTODIR=$1

cp $PROTODIR grpc-lib/src/main/proto/
cd grpc-lib && mvn install
mv target/grpc-lib-0.0.1.jar ../apache-jmeter-5.2/lib/ext/
mvn clean && cd ..