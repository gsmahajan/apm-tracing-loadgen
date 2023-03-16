#! /bin/bash


AWS_ACCOUNT_ID="148849679107"
AWS_ECR_LINK=$(echo $AWS_ACCOUNT_ID".dkr.ecr.us-west-2.amazonaws.com")

init_aws(){

# you need this your own
. ~/.aws_creds.sh

# connect kubectl to cluster
aws eks --region us-west-2 update-kubeconfig --name LMAPMLoadGenerateTopology

# connect docker cli to eks clusters registry
aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin $AWS_ECR_LINK

}


function common {
 
. ~/.lmenv
# echo "Enter below details"

#while true; do
#    echo -n "Do you want to run the demo in cloud aws ? [yY/nN] : "
#    read awsInit
#    if [[ $awsInit =~ "N|n" ]]; then
#	continue        
#    else
#      log "using ecr link - $AWS_ECR_LINK"
#      init_aws
#      break
#    fi
#done


#export AWS_SECRET_ACCESS_KEY="Foo"
#init_aws

}

common
