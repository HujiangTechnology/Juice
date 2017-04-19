For [ENGLISH](README_en.md)
## Why Juice
juice是沪江(hujiang)学习系统项目组(LMS)所开发的一套基于Mesos Framework的分布式任务调度云系统，基于此系统，可以实现任何作业型任务的调度工作。

耗时计算型任务处理在沪江一直是个持续的需求，例如多媒体转码，Map Reduce，密集计算等等任务。在Juice出现以前，各个项目组各显神通，开发着不同的任务处理系统，不但耗费人力，还特别耗费服务器。
Juice利用Mesos集群空闲的计算能力，负责统一的接口返回和任务查询功能，其优点如下：
* 基于Mesos集群，最大程度利用空闲资源完成耗时计算
* 异步调度，并提供结果查询和回调两种方法获取执行结果
* 分布式管理，所有耗时任务都在资源池中无状态调度。
* 任务查询，目前只开放通过接口查询任务的状态和结果。


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
* 安装zookeeper集群
* 安装Mesos集群，并加入Zookeeper集群中。

~~~~
  配置zookeeper地址到项目配置文件中:
  1.juice-service/src/main/resources/application-dev.properties
  mesos.framework.zk=your_zk1_ip:port,your_zk2_ip:port,your_zk3_ip:port,your_zk4_ip:port,your_zk5_ip:port
  PS juice-service通过zookeeper获取mesos master地址，所以必须将mesos集群配置到zookeeper中进行HA切换。
~~~~

* 安装mysql5.6以上版本
  
~~~~
    创建mysql数据juice,schema为juice。
    执行/script/juice_2017-V1.0-OPEN.sql创建2张表juice_framework、juice_task。
    修改以下内容：
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
    将以上内容修改为数据库安装时的配置信息。
~~~~

* 安装Redis3.0以上版本

~~~~
    修改以下内容：
    1.juice-rest/src/main/resources/config/application-dev.properties
    spring.redis.host=redis_ip
    spring.redis.port=redis_port
    spring.redis.password=
    2.juice-service/src/main/resources/application-dev.properties
    redis.host=redis_ip
    redis.port=redis_port
    redis.password=
    将以上内容修改为Redis安装时的配置信息。
~~~~

~~~~
在项目根目录执行mvn clean install编译项目，编译后产生juice-service和juice-rest2个jar包。
项目启动:
* java -Dsystem.environment=dev -jar juice-service.jar
* java -Dspring.profiles.active=dev -jar juice-rest.jar
~~~~

## How To Use Juice###
* [API Document](doc/api_document.md)
* [Juice_SDK示例](doc/juice_sdk_example.md)
* [参数说明文档](doc/params.md)

创建任务步骤:
* 开发能够使用Docker启动的任务（主业务逻辑）并PUSH到Docker镜像仓库。
* 调用Juice API发起任务
* 等待回调结果，或主动fetch结果

~~~~
Juice最适合运行Docker任务,通常做法将Jar包打入一个Docker镜像，提交此镜像到dockerhub上，提交一个Container模式的任务到到Juice,其中参数dockerImage为Docker镜像名。
Juice会自动的寻找一个合适的Agent来执行此任务，调用方不必关心在哪个Host上执行，而只需要关心任务是否执行成功即可。
~~~~



