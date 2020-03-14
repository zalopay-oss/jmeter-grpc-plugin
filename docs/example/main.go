package main

import (
	"flag"
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"
)

/*
* This main function call script compile proto file
* run jmeter testing and finally generate the report
*/

func main() {

	var (
		protoFile = flag.String("p", "./hello.proto", "proto file ")
		jmxFile   = flag.String("t", "./hello.jmx", "test script file")
		logFile   = flag.String("j", "./hello.log", "jmeter log file")
		csvOut    = flag.String("l", "./hello.csv", "csv result file")
		reportOut = flag.String("o", "./hello_report", "folder contain report")
	)

	flag.Parse()

	var args = []string{
		fmt.Sprintf("scripts/compile.sh %s", *protoFile),
		fmt.Sprintf("apache-jmeter-5.2/bin/jmeter.sh -n -t %s -j %s -l %s", *jmxFile, *logFile, *csvOut),
		fmt.Sprintf("apache-jmeter-5.2/bin/jmeter.sh -g %s -o %s", *csvOut, *reportOut),
	}

	cmd := exec.Command("/bin/sh", "-c", strings.Join(args, ";"))
	cmd.Stdout = os.Stdout
	err := cmd.Start()

	if err != nil {
		log.Fatal(err)
	}

	err = cmd.Wait()

	if err != nil {
		log.Fatal(err)
	}
}
