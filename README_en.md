## Why Juice

Juice is a set of distributed task scheduling cloud system based on Mesos Framework developed by Hujiang Learning Management System group (LMS). Based on this system you can realize the scheduling of any job task.

Time-consuming computing tasks in Hujiang is a continuous demand. Such as multimedia transcoding, Map Reduce, intensive computing and so on. Each working team were do their own before Juice, do the development of different task processing systems, not only consuming the manpower, but also consuming the server.

Juice uses Mesos clustered idle computing capabilities, responsible for a unified interface return and task query function, the advantages are as follows:

* Based on the Mesos cluster, the maximum use of idle resources to complete time-consuming calculation
* Asynchronous scheduling, and provide results query and callback to obtain the results of the implementation
* Distributed management, all time-consuming tasks are stateless in resource pools.
* Task query, currently opened the interface for query task status and results.

##### Latest Release Version:1.0-OPEN

## Getting Started

##### Requirements

* [Mesos](http://mesos.apache.org/gettingstarted/)
* [ZooKeeper](https://zookeeper.apache.org/doc/r3.4.6/zookeeperStarted.html)
* Java8
* [MySql](https://dev.mysql.com/doc/mysql-getting-started/en/)
* [Redis](https://redis.io/)
* [Marathon](https://mesosphere.github.io/marathon/)(optional)
* [Docker](https://www.docker.com/)

## Install

* Install the Zookeeper cluster
* Install the Mesos cluster and join the Zookeeper cluster.

~~~~
  Add Zookeeper's address to the configuration file of project:

  juice-service/src/main/resources/application-dev.properties
  mesos.framework.zk=your_zk1_ip:port,your_zk2_ip:port,your_zk3_ip:port,your_zk4_ip:port,your_zk5_ip:port

  NOTE: juice-service will get Mesos Master's address through Zookeeper, must add Mesos cluster to Zookeeper cluster to do the HA switch.
~~~~

* Install mysql5.6 or later

~~~~
    Create mysql database juice and schema as juice.
    Run /script/juice_2017-V1.0-OPEN.sql to create tables juice_framework and juice_task.

    Modify the configuration:

    1.juice-jooq/pom.xml：
    <jdbc>
        <driver>com.mysql.jdbc.Driver</driver>
        <url>jdbc:mysql://your_ip:port/juice</url> <!-- ip & port & database name -->
        <user>user</user> <!-- username of database -->
        <password>password</password> <!-- password of database -->
    </jdbc>

    2.juice-rest/src/main/resources/config/application-dev.properties
        spring.datasource.url=jdbc:mysql://your_ip:port/juice?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&useSSL=false
        spring.datasource.username=user
        spring.datasource.password=password

    3.juice-service/src/main/resources/application-dev.properties
        db.url=jdbc:mysql://your_ip:port/juice?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&useSSL=false
        db.user=user
        db.password=password

~~~~

* Install Redis3.0 or later

~~~~
    Modify the configuration:

    1.juice-rest/src/main/resources/config/application-dev.properties
        spring.redis.host=redis_ip
        spring.redis.port=redis_port
        spring.redis.password=

    2.juice-service/src/main/resources/application-dev.properties
        redis.host=redis_ip
        redis.port=redis_port
        redis.password=

~~~~

~~~~
Run mvn clean install at root path of project and compile. juice-service and juice-rest will be generated after compile.

Start Project:

* java -Dsystem.environment=dev -jar juice-service.jar
* java -Dspring.profiles.active=dev -jar juice-rest.jar
~~~~

## How To Use

* [API Document](doc/api_document.md)
* [Juice_SDK Sample](doc/juice_sdk_example.md)
* [Parameter Description](doc/params.md)

Step of creating task:

* Develop the ability to use Docker to start the task (main business logic) and push to Docker repository.
* Call Juice API with the task description(BODY)
* Wait for the results callback or fetch results manually.

~~~~
Juice is best suited for running Docker tasks, and it is common practice to put a Jar package into a Docker image. Submit the mirror to Dockerhub and submit a container mode task to Juice where the dockerImage parameter is the Docker mirror name.

Juice will automatically find a suitable Agent to perform this task, the caller does not have to care about which host to perform on, but only need to care about whether the task can be successful.
~~~~
