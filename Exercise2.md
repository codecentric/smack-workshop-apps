### Exercise 2: Installing Kafka in DC/OS and create a topic

In this exercise, we are going to setup a Kafka Cluster in DC/OS. Then we’ll connect to it via Kafka command line tools to verify that it is working

1. Open the DC/OS web ui
4. Go to Universe
5. Search for "kafka"
6. Select "kafka" (not confluent-kafka!) and select advanced installation
7. Select "brokers" on the left and enter "9092" as value for port, then install
8. Check the progress in your Web UIs
9. Once the Kafka Service is healthy, we create a Kafka topic:
   - dcos kafka topic create spotify --partitions 3 --replication 2

Testing Kafka from outside the cluster is tricky. These optional exercises download a local Kafka distribution to your VM to demonstrate message production and consumation.


1. Change into the kafka directory﻿ /home/smack/tools/kafka_2.11-0.10.0.1 (your going to need four shells in that folder)”
2. In the first shell, start Zookeeper with “bin/zookeeper-server-start.sh config/zookeeper.properties”
3. In the second shell, start a Kafka broker with “bin/kafka-server-start.sh config/server.properties”
4. In the third shell, create a Kafka topic “bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic manual-test”
5. In the same shell, start a Kafka Consumer with “bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --new-consumer --topic manual-test”
6. In the fourth shell, start a Kafka Producer with “bin/kafka-console-producer.sh --broker-list localhost:9092 --topic manual-test” and produce some messages
7. Check in the third shell if the messages arrive
