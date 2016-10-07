### Exercise 3: Building an Ingestion applications with Akka Streams

In this exercise, you will complete an Ingestion application, upload it to hub.docker.io and deploy it in the cluster

1. Open build.sbt and replace the version with an identifier that uniquely identifies your group
2. Go to src/main/scala/de.codecentric.smack/Client and implement the methods “mapIncomingWSMessage” and “mapOptionalTrackToProducerRecord”. The Scaladoc gives you hints on how to achieve it
3. From the ingest root directory, run sbt test and make sure all tests pass
4. Login into the docker registry with “sudo docker login”
   - User “smackatdata2day”
   - Pw “Sm@ckWork!”
5. Build and Push your application with “sudo sbt -Dsbt.ivy.home=/home/smack/.ivy2 dockerBuildAndPush”
6. After this has finished, open ingest.marathon.mesos and replace the ??? with the version you set in build.sbt
7. Install the app with “dcos marathon app add ingest.marathon.mesos”
8. Explore the logs of your ingest app for errors
