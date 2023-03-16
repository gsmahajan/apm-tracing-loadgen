# `APM Tracing Loadgen Tool - LogicMonitor India LLP` -

# Logic in Nutshell
=> read the file src/main/resources/data.sql and populate those many services into the EKS cluster based on feeded nodes to the cluster.

## File use to get spans associated tags are in tags-combined-http.txt 
- total tags send along with one span == 1000 considering one span in one trace. )

## File use to get the number of services to be swapn in EKS are in as following.
- data.sql (default eks having 50 services)
- data-localdev (default for minikube having 10 services spans ) 
- data-50.sql is == data.sql
- data-100.sql is to deal with 100 services mimics Netflix like backend.
- data-1000.sql is having total of 1665 massive services mimics Walmark like backend.


(mvn clean verify is attached to kubernetes / docker running in minikube)

## Steps to follow - (you shall have kubectl / aws / docker clis knows each other, and working jdk / mvn utility too)
```bash
git clone gsmahajan/apm-tracing-loadgen
```

* 2.a cat ~/.lmenv # chmod 755 ~/.lmenv
```bash
# ! /bin/bash

# these credentials created when you signup with the portal in Settings -> Users -> Profile section
export LOGICMONITOR_ACCOUNT="<your-company-name>"
export LOGICMONOTOR_BEARER_TOKEN="<your-companies-your-account-apm-bearer-token>"


# these keys created when you enroll an OpenTelemetry collector from LM Modules (so telemetry identified valid in-comings)
export LOGICMONITOR_ACCESS_ID="<logicmonitor-collector-otel-accessId>"
export LOGICMONITOR_ACCESS_KEY=<logicmonitor-collector-otel-accessKey>"

export OTEL_EXPORTER_OTLP_ENDPOINT="https://$(echo "$LOGICMONITOR_ACCOUNT").logicmonitor.com/santaba/rest/api"

```
* 2.b cat ~/.aws_creds.sh # new file chmod 755 ~/.aws_creds.sh # Be careful here

```bash

export AWS_ACCESS_KEY_ID="<aws-your-company-vpc-access-id>"
export AWS_SECRET_ACCESS_KEY="<aws-your-company-vpc-access-key>"
export AWS_SESSION_TOKEN="<aws-your-company-vpc-session-token>"

# see common.sh, FIXME it has been commented for minikube branch
export AWS_ACCOUNT_ID="<your-company-account-id>"
export AWS_REGION="us-west-2"
export AWS_ECR_LINK=$(echo $AWS_ACCOUNT_ID".dkr.ecr.$AWS_REGION.amazonaws.com")


```
* 2.c cat ./multitenant-build.sh # starting point (use light version first with one company then add the umbrella of sis companies)
```bash
  * for company in localdev elppa tfosorcim elgoog sysinfo prowe; do
# run this in backgroud if using for multiple companies
./build-eks.sh $company
done

```
~ Note that the common.sh has some pilot code which is specific to developer friendly. so need to have some stuff added later on. thanks


# WIP -> 
* 3 kubectl create namespae punedev-$yourcompanyName (so a new namespace is going to be created in kubernetes cluster,
  have the nodegroup as per the sizing you required - ( to a joke, it may sounds, minikube, slim / fat / heavy / duplex
  / xxl /xxxl etc that LM managmnt may work out with before closing)
* 4 edit src/main/resources/data.sql to frame number of mock applications needs to spin. the image in the data.sql is
  expected 2b and availbl in ecr. In this step you are actually mimicing the customer environemnt plot (what services /
  tiers / complexities he has and how many will he interacts with and the traffic-rate it expects LM to sustain to be
  precise with actual challenge that LM has)
* 5 aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin <accountId>
  .dkr.ecr.<region>amazonaws.com (push it)
* 8 log in to your logicmonitor account and open the Traces tab that shows the traces / spans getting ingested while the
  apps getting started.
* 9 once done, close the program that spins kubelets on cluster, verify the cluster kubectl get po -n punedev

```

#### add or remove service on the go along with config required to decide for tracing platform (how many it links, how many traces / spans it has to generate and are there custom tags required specifically)  crud - see data.sql

`curl -i http://localhost:8080/apps`

#### fetch kubernetes services created

`curl -i http://localhost:8080/orchestration/87a96e6fe51c0dcc8068f3cec879141a6219c988ede07eaf7291b53fcf78f2bf/getKubeServiceNames`

#### create kubernetes pods / service

`curl -i http://localhost:8080/orchestration/87a96e6fe51c0dcc8068f3cec879141a6219c988ede07eaf7291b53fcf78f2bf/create?ns=tactusdemo&appName=operatorProvisioningGateway&port=9343&image=mockapp:1.18`

#### delete kubernetes pods /services

TODO

#### clean slate kubernetes

kubectl delete namespace punedev

#### loadgen tool config crud (don't trust)

TODO

#### to stop loadgen (don't trust)

`touch /opt/logicmonitor/apmloadgen/break.txt`

#### initiate loadgen (starting spans generation) for the devteam (don't trust)

TODO

#### h2 console reading in memory rdbms data for initially begining (working)

`http://localhost:8080/h2-console`
`datasource url : jdbc:h2:mem:lmapmloadgen`
`username : sa`
`password : password`

Phase II -  <<TODO>> (no time)

create a CLI around this service that can be used by developers for everyday tasks.

Important! : Close the cluster you created post using the platform, otherwise AWS gets ahead of Tesla in the
meanwhile. :)
