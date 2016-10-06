### Exercise 1: Deploying an Application on DC/OS

In this exercise, we are going to deploy the Websocket server on our DC/OS cluster

1. Go to http://<public ip of your master node> to see the DC/OS Dashboard
2. Go to “Services” and click on “Deploy Service”
3. Flip the Switch on the top right of the popup window to see “JSON mode”
4. Replace the content of the window with the content of websocket.marathon.json at the root of your ingest project
5. Deploy the application
6. Explore
   - http://<public ip of your master node>
   - http://<public ip of your master node>/marathon
   - http://<public ip of your master node>/mesos
7. When the Websocket application has been deployed in your cluster, open ingest/websocket-test.html and replace the IP address in line 6 with the public IP address of your Public Slave
8. Open the html file in a browser and check if Websocket messages are coming in

Now we install Cassandra and Spark

1. Open http://<public ip of your master node>
2. Go to Universe
3. Search for "kafka"
4. Select "kafka" (not confluent-kafka!) and select advanced installation
5. Select "brokers" on the left and enter "9092" as value for port, then install
6. Open a shell
7. Type “dcos package list” – this will fail as we have not connected our DC/OS-CLI with a cluster
8. Issue “dcos config set core.dcos_url http://<public IP of your master node>
9. To demonstrate installation from the command line, go to your VM and type `dcos package install spark --package-version=﻿1.0.1-1.6.2` to install Spark
