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
