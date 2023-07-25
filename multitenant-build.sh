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

for company in localdev barbara texastech whutechai; do 

 # run this in backgroud if using for multiple companies, this produces number of services per company in the larger umbrella.
 ./build-eks.sh $company   

done
