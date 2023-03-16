#! /bin/bash

#!/bin/bash -x

#        ####   ####  #  ####  ##   ##  ####  #    # # #####  ####  ##### 
#       #    # #    # # #    # # # # # #    # ##   # #   #   #    # #    #
#       #    # #      # #      #  #  # #    # # #  # #   #   #    # #    #
#       #    # #  ### # #      #     # #    # #  # # #   #   #    # ##### 
#       #    # #    # # #    # #     # #    # #   ## #   #   #    # #   #
#######  ####   ####  #  ####  #     #  ####  #    # #   #    ####  #    #
                                                                                                                               
# @Copyright LogicMonitor India LLP, 2023

# aws eks remove node group
function deleteNodeGroup {
. /Users/$(whoami)/.aws_cred.sh

 aws eks --region us-west-2 update-kubeconfig --name LMAPMLoadGenerateTopology
 aws eks --region us-west-2 delete-nodegroup --cluster-name LMAPMLoadGenerateTopology --nodegroup-name lmapmloadgen --debug --output text

}

deleteNodeGroup
