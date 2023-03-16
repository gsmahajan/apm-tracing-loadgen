#! /bin/bash -x

#                              #     #                                     #     #                                             
#        ####   ####  #  ####  ##   ##  ####  #    # # #####  ####  #####  #     # ###### # #    # #####    ##   #      #      
#       #    # #    # # #    # # # # # #    # ##   # #   #   #    # #    # #     # #      # ##  ## #    #  #  #  #      #      
#       #    # #      # #      #  #  # #    # # #  # #   #   #    # #    # ####### #####  # # ## # #    # #    # #      #      
#       #    # #  ### # #      #     # #    # #  # # #   #   #    # #####  #     # #      # #    # #    # ###### #      #      
#       #    # #    # # #    # #     # #    # #   ## #   #   #    # #   #  #     # #      # #    # #    # #    # #      #      
#######  ####   ####  #  ####  #     #  ####  #    # #   #    ####  #    # #     # ###### # #    # #####  #    # ###### ###### 
                                                                                                                               
# @Copyright LogicMonitor India LLP, 2023

# TEAM APM - PUNEDEV

HOMEDIR="/Users/$(whoami)/dev/apm-tracing-loadgen"

company="$1"

[ -z "$company" ] && company="localdev"

kubenamespace="punedev-$company"

# Make sure you do not<F3> u
. $HOMEDIR/common.sh


echo "displaying list of images"


function cleanup {
 kubectl delete namespace $kubenamespace
 sleep 3
 
 #for file in $(ls /tmp/foo/*.yaml); do kubectl delete -f $file -n $kubenamespace ; done
 rm -rf /tmp/foo && mkdir /tmp/foo
 kubectl create namespace $kubenamespace
}

function packageJava {
   mvn clean package -Dlmapmloadge.manufmockapps.skip=true -Dmaven.test.skip=true
}


function buildImage {
 docker build . -t apmloadgen:1.18
 docker tag apmloadgen:1.18 apmloadgen:1.18
# docker push apmloadgen:1.18
 docker pull logicmonitor/lmotel:latest
}


function startRootApp {
 mvn spring-boot:run -Dspring-boot.run.jvmArguments="-DMANUFACTURE_COMPANY_NAME=$company -DOTEL_COLLECTORS_TO_SPIN=1"
}


function main {

 cleanup
 packageJava
 buildImage
 
 # forcing it to go native localdev as by the time build image works this shall download the collector from qa envs. ;)
 # forge kubernetes
 startRootApp # therte is a bug in code that does not stops the tomcat post running.

 
 
 # see which image are being referred
 # curl -sSi localhost/apps | grep image| sort | uniq | sed -e 's/.*: "//g' -e 's/".*//g'

}

main

