# Example

## Overview

This example contains utility commands, help you run test gRPC with Jmeter easily.

## Prerequisites

* Golang (version >= 1.12.4)
* Maven (version >= 3.6.0)

## Usage



```sh
# grant role execute shell script
$ sudo chmod +x scripts/*

# download apache-jmeter-x.x.zip
$ scripts/download.sh

# install grpc jmeter plugin
$ scripts/install.sh

# run your jmeter test script
$ go run main.go -p <protofile> -t <jmxfile> -l <csv contain metrics> -o <folder name contain report>
```

### Example

* Run server :

```sh
# run greeter server
$ scripts/runserver.sh
```

* Run test :

```sh
# run example
$ go run main.go -p ./hello.proto -t ./hello.jmx -l ./hello.csv -o ./hello_report
```