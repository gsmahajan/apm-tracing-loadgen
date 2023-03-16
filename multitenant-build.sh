#!/bin/bash -x

#                              #     #                                     #     #                                             
#        ####   ####  #  ####  ##   ##  ####  #    # # #####  ####  #####  #     # ###### # #    # #####    ##   #      #      
#       #    # #    # # #    # # # # # #    # ##   # #   #   #    # #    # #     # #      # ##  ## #    #  #  #  #      #      
#       #    # #      # #      #  #  # #    # # #  # #   #   #    # #    # ####### #####  # # ## # #    # #    # #      #      
#       #    # #  ### # #      #     # #    # #  # # #   #   #    # #####  #     # #      # #    # #    # ###### #      #      
#       #    # #    # # #    # #     # #    # #   ## #   #   #    # #   #  #     # #      # #    # #    # #    # #      #      
#######  ####   ####  #  ####  #     #  ####  #    # #   #    ####  #    # #     # ###### # #    # #####  #    # ###### ###### 
                                                                                                                               
# @Copyright LogicMonitor India LLP, 2022

#./clean-slate.sh

for company in localdev; do 

 # run this in backgroud if using for multiple companies 
 ./build-eks.sh $company   

done
