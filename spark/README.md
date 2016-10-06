# smack-workshop-spark-app

## Kafka

Zookeeper starten

```
bin/zookeeper-server-start.sh config/zookeeper.properties
```

Kafka starten

```
bin/kafka-server-start.sh config/server.properties
```

## Cassandra

Cassandra starten

```
bin/cassandra -f
```

CQLSH starten

```
bin/cqlsh
```


## Tunnel Cassandra

ssh -i <<pfad/zur/pem>> core@<<masterIp>> -L 9042:node-0.cassandra.mesos:9042
