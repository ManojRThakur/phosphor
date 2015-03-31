#!/bin/bash

DIR=$(pwd)

JRE_INST_HOME=$1
MAIN_CLASS=$2
BM=$3

JRE_INST_HOME=$(cd $JRE_INST_HOME && pwd)
cd $DIR

if [ -z $BM ]; then # run all instrumented benchmarks
    for instrumented in *-inst/; do
        BM=$( echo $(basename $instrumented) | sed "s/-inst$//" )

        if [ "$BM" != "jre" ] && [ "$BM" != "partial" ]; then
            echo "Running '$BM'..."
            cd $instrumented
            $JRE_INST_HOME/bin/java -Xdebug -Xbootclasspath/a:../phosphor_pi.jar -javaagent:../phosphor_pi.jar $MAIN_CLASS $BM
        fi
    done
else
    if [ -d "$BM-inst" ]; then
        cd "$BM-inst"
        $JRE_INST_HOME/bin/java -Xdebug -Xbootclasspath/a:../phosphor_pi.jar -javaagent:../phosphor_pi.jar $MAIN_CLASS $BM
    else
        echo "Benchmark '$BM' has not been instrumented"
    fi
fi