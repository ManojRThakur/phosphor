#!/bin/bash

if [ -z "$1" ]; then
    echo "$0: provide a benchmark"
    exit 1
fi

bm=$1

ROOT=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

PHOSPHOR_HOME=$ROOT
PETABLOX_HOME=$( cd $ROOT && cd .. && pwd )/petablox
DOOP_HOME=$PETABLOX_HOME/doop-r160113-bin
JRE_INST_HOME=$PHOSPHOR_HOME/jre-inst

# Step 1: Generate methods_inst using doop and cp to $PHOSPHOR_HOME
cd $PETABLOX_HOME

vagrant up
vagrant ssh -c "cd /vagrant/doop-r160113-bin && mkdir -p logs && ./exec-dacapo.sh $bm > logs/$bm.log"
cp $DOOP_HOME/ucla-pls/methods_inst $PHOSPHOR_HOME/methods

# Step 2: Run phosphor_pi.jar
rm -r $PHOSPHOR_HOME/$bm-inst > /dev/null
cd $PHOSPHOR_HOME
./instrument.sh $bm
./run-instrumented.sh $JRE_INST_HOME Harness $bm
