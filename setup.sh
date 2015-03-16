#!/bin/bash

if [ -z "$JAVA_HOME" ]; then
    echo "ERR: JAVA_HOME must be set"
    exit 1;
fi

if [ -z "$1" ]; then
    echo "$0: please provide dacapo-2009.tar.gz location"
    exit 1;
fi

$DACAPO=$1

ROOT=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cp $DACAPO $ROOT

PHOSPHOR_HOME=$ROOT
PETABLOX_HOME=$( cd $ROOT && cd .. && pwd )/petablox
DOOP_HOME=$PETABLOX_HOME/doop-r160113-bin
JRE_INST_HOME=$PHOSPHOR_HOME/jre-inst

# Step 1: Setup dacapo jars in phosphor folder && doop folder
cd $ROOT
$scratch=$(mktemp -d XXXXXXXX)
mv dacapo-2009.tar.gz $scratch
cd $scratch

tar -xzf dacapo-2009.tar.gz
rm dacapo-2009/.DS_Store > /dev/null

cp -R dacapo-2009/ $DOOP_HOME

for bm in dacapo-2009/*; do
    rm -r $PHOSPHOR_HOME/$bm > /dev/null
    mkdir -p $PHOSPHOR_HOME/$bm
    cp $DOOP_HOME/dacapo-2009/$bm/$bm.jar $PHOSPHOR_HOME/$bm/
    cd $PHOSPHOR_HOME/$bm; jar xf $bm.jar
done

cd $ROOT
rm -rf $scratch

# Step 2: Provision VM correctly
vagrant up
vagrant ssh -c 'sudo apt-get update && sudo apt-get upgrade'
vagrant provision

# Step 3: Instrument JVM
cd $PHOSPHOR_HOME
java -jar phosphor.jar $JAVA_HOME/jre $JRE_INST_HOME