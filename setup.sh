#!/bin/bash

if [ -z ${1+x} ]; then
    STEP=0
else
    STEP=$1
fi

if [ -z "$JAVA_HOME" ]; then
    echo "ERR: JAVA_HOME must be set"
    exit 1;
fi

ROOT=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

PHOSPHOR_HOME=$ROOT
PETABLOX_HOME=$( cd $ROOT && cd .. && pwd )/petablox
DOOP_HOME=$PETABLOX_HOME/doop-r160113-bin
JRE_INST_HOME=$PHOSPHOR_HOME/jre-inst

if (( "$STEP" <= 1)); then
# Step 1: Download 2009 Dacapo jars
echo "Downloading DaCapo jars from BitBucket..."
echo -n "Enter your BitBucket username: "; read username
echo -n "Enter your BitBucket password: "; read -s pw

cd $ROOT
curl -L -u $username:$pw -O https://bitbucket.org/mayurnaik/petablox/downloads/dacapo-2009.tar.gz
DACAPO=$ROOT/dacapo-2009.tar.gz
fi

if (( "$STEP" <= 2 )); then
# Step 2: Setup dacapo jars in phosphor folder && doop folder
cd $ROOT
scratch=$(mktemp -d XXXXXXXX)
mv $DACAPO $scratch
cd $scratch

tar -xzf dacapo-2009.tar.gz
rm dacapo-2009/.DS_Store > /dev/null

cp -R dacapo-2009 $DOOP_HOME

for bm in dacapo-2009/*; do
        bm=$(basename $bm)
        if [ "$bm" != "common-deps" ]; then
                if [ -d "$PHOSPHOR_HOME/$bm" ]; then
                    rm -r $PHOSPHOR_HOME/$bm > /dev/null
                fi
            cd $DOOP_HOME/dacapo-2009/$bm; jar xf $bm.jar
            cp -R $DOOP_HOME/dacapo-2009/$bm $PHOSPHOR_HOME
        fi
done

cd $ROOT
rm -rf $scratch
fi

if (( "$STEP" <= 3 )); then
# Step 3: Provision VM correctly
cd $PETABLOX_HOME/provision
cp config.json.default config.json

cd $DOOP_HOME/externals

curl -L -u $username:$pw -O https://bitbucket.org/mayurnaik/petablox/downloads/jre-6u45-linux-x64.bin -O https://bitbucket.org/mayurnaik/petablox/downloads/jre-1_5_0_22-linux-amd64.bin  -O https://bitbucket.org/mayurnaik/petablox/downloads/j2re-1_4_2_19-linux-i586.bin -O https://bitbucket.org/mayurnaik/petablox/downloads/j2re-1_3_1_20-linux-i586.bin -O https://bitbucket.org/mayurnaik/petablox/downloads/dacapo-2006-10-MR2-xdeps.zip -O https://bitbucket.org/mayurnaik/petablox/downloads/logicblox-3.10.21.tar.gz
cd $PETABLOX_HOME
mv $DOOP_HOME/externals/logicblox-3.10.21.tar.gz .
tar -xzf logicblox-3.10.21.tar.gz
rm logicblox-3.10.21.tar.gz
fi

if (( "$STEP" <= 4 )); then
# Step 4: Instrument JVM
cd $PHOSPHOR_HOME
java -jar phosphor.jar $JAVA_HOME/jre $JRE_INST_HOME
chmod +x $JRE_INST_HOME/bin/java
fi

if (( "$STEP" <= 5 )); then
# Step 5: Provision VM, destroy any previously created VM
cd $PETABLOX_HOME
vagrant destroy
vagrant up
vagrant ssh -c 'sudo apt-get -y update && sudo apt-get -y upgrade'
vagrant provision
fi