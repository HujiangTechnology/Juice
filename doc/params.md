### Params 
Juice所有的参数均已设置默认值，同时也提供用户自定义输入参数参数值，只需在配置文件中加个具体参数值即可

#### Juice Rest
设置参数到(dev)：
juice-rest/src/main/resources/config/application-dev.properties

~~~~
#任务处理队列,默认为juice.task.queue.dev,如设置,必须和juice-service中设置相同
juice.task.queue＝ 
#任务结果队列,默认为juice.task.result.queue.dev,必须和juice-service中设置相同
juice.task.result.queue=
#任务管理队列,默认为juice.management.queue,必须和juice-service中设置相同
juice.management.queue=
#任务在队列中的过期时间,默认86400,如设置,必须和juice-rest中设置相同
juice.task.expired.of.seconds=
~~~~

#### Juice Service
设置参数到(dev)：
juice-service/src/main/resources/application-dev.properties

~~~~
#Zookeeper集群地址
mesos.framework.zk＝
#是否使用zookeeper进行HA,默认true
zookeeper.distribute.lock.ha=
#framework的名字,默认以juice-service-dev开头
mesos.scheduler.name=
#单个Agent资源分配阀值,默认0.8
resources.use.threshold=
#指定Mesos的Attribute,使juice-service只使用mesos集群中的某些资源
#比如ocs|ams,qa,mid
mesos.framework.attr=
#send-pool-size,默认20
send.pool.size=
#auxiliary-pool-size,默认20
auxiliary.pool.size=
＃max reserved，默认1024，任务重试次数
max.reserved=
#framework的标记,默认mesos.framework.tag.dev
mesos.framework.tag=
#任务处理队列,默认为juice.task.queue.dev,如设置,必须和juice-rest中设置相同
juice.task.queue＝ 
#任务结果队列,默认为juice.task.result.queue.dev,如设置,必须和juice-rest中设置相同
juice.task.result.queue=
#任务管理队列,默认为juice.management.queue,如设置,必须和juice-rest中设置相同
juice.management.queue=
#重试队列,默认为juice.task.retry.queue.dev
juice.task.retry.queue=
#任务在队列中的过期时间,默认86400,如设置,必须和juice-rest中设置相同
juice.task.expired.of.seconds=
~~~~
