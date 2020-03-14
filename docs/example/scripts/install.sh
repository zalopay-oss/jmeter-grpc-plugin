#!/bin/sh

cd ../..
mvn clean install
mv target/jmeter-grpc-client-sampler.jar docs/example/apache-jmeter-5.2/lib/ext/
