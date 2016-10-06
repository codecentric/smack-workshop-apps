### Exercise 2: Installing Kafka in DC/OS and create a topic

In this exercise, we are going to setup a Kafka Cluster in DC/OS. Then we’ll connect to it via Kafka command line tools to verify that it is working

1. Open a shell
2. Type “dcos package list” – this will fail as we have not connected our DC/OS-CLI with a cluster
3. Issue “dcos config set core.dcos_url http://<public IP of your master node>
4. Now it is time to install Kafka – execute “dcos package install kafka”
5. Check the progress in your Web UIs
6. Once the Kafka Service is healthy, we create a Kafka topic:
   - dcos kafka topic create spotify --partitions 3 --replication 2

Testing Kafka from outside the cluster is tricky. These optional exercises download a local Kafka distribution to your VM to demonstrate message production and consumation.

1. From your home directory, call “wget http://mirrors.ae-online.de/apache/kafka/0.10.0.1/kafka_2.11-0.10.0.1.tgz”
2. Extract the archive with “tar xvf kafka_2.11-0.10.0.1.tgz”
3. Change into the kafka directory (your going to need four shells in that folder”
4. In the first shell, start Zookeeper with “bin/zookeeper-server-start.sh config/zookeeper.properties”
5. In the second shell, start a Kafka broker with “bin/kafka-server-start.sh config/server.properties”
6. In the third shell, create a Kafka topic “bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic manual-test”
7. In the same shell, start a Kafka Consumer with “bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --new-consumer --topic manual-test”
8. In the fourth shell, start a Kafka Producer with “bin/kafka-console-producer.sh --broker-list localhost:9092 --topic manual-test” and produce some messages
9. Check in the third shell if the messages arrive
