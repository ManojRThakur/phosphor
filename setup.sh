#!/bin/bash

if [ -z "$JAVA_HOME" ]; then
    echo "ERR: JAVA_HOME must be set"
    exit 1;
fi

if [ -z "$1" ]; then
    echo "$0: please provide dacapo-2009.tar.gz location"
    exit 1;
fi

DACAPO=$1

ROOT=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cp $DACAPO $ROOT

PHOSPHOR_HOME=$ROOT
PETABLOX_HOME=$( cd $ROOT && cd .. && pwd )/petablox
DOOP_HOME=$PETABLOX_HOME/doop-r160113-bin
JRE_INST_HOME=$PHOSPHOR_HOME/jre-inst

# Step 1: Setup dacapo jars in phosphor folder && doop folder
cd $ROOT
scratch=$(mktemp -d XXXXXXXX)
mv dacapo-2009.tar.gz $scratch
cd $scratch

tar -xzf dacapo-2009.tar.gz
rm dacapo-2009/.DS_Store > /dev/null

cp -r dacapo-2009 $DOOP_HOME

for bm in dacapo-2009/*; do
	bm=$(basename $bm)
	if [ "$bm" != "common-deps" ]; then
		if [ -d "$PHOSPHOR_HOME/$bm" ]; then
		 	rm -r $PHOSPHOR_HOME/$bm > /dev/null
		fi
	    mkdir -p $PHOSPHOR_HOME/$bm
	    cp $DOOP_HOME/dacapo-2009/$bm/$bm.jar $PHOSPHOR_HOME/$bm/
	    cd $PHOSPHOR_HOME/$bm; jar xf $bm.jar
	fi
done

cd $ROOT
rm -rf $scratch

# Step 2: Provision VM correctly
cd $PETABLOX_HOME/provision 
cp config.json.default config.json

cd $DOOP_HOME/externals
echo "Enter your bitbucket username:"
read username
curl -L -su $username -O https://bitbucket.org/mayurnaik/petablox/downloads/jre-6u45-linux-x64.bin -O https://bitbucket.org/mayurnaik/petablox/downloads/jre-1_5_0_22-linux-amd64.bin  -O https://bitbucket.org/mayurnaik/petablox/downloads/j2re-1_4_2_19-linux-i586.bin -O https://bitbucket.org/mayurnaik/petablox/downloads/j2re-1_3_1_20-linux-i586.bin -O https://bitbucket.org/mayurnaik/petablox/downloads/dacapo-2006-10-MR2-xdeps.zip -O https://bitbucket.org/mayurnaik/petablox/downloads/logicblox-3.10.21.tar.gz
cd $PETABLOX_HOME
mv $DOOP_HOME/externals/logicblox-3.10.21.tar.gz .
tar -xzf logicblox-3.10.21.tar.gz 
rm logicblox-3.10.21.tar.gz
# destroy any previously created VM
vagrant destroy
vagrant up
vagrant ssh -c 'sudo apt-get update && sudo apt-get upgrade'
vagrant provision

# Step 3: Instrument JVM
cd $PHOSPHOR_HOME
java -jar phosphor.jar $JAVA_HOME/jre $JRE_INST_HOME