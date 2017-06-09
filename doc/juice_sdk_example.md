##### Juice client sdk采用java1.8编写，Maven依赖：

    <dependency>
        <groupId>com.hujiang</groupId>
        <artifactId>juice-client-sdk</artifactId>
        <version>1.0-OPEN</version>
    </dependency>
    

<pre>
提交一个Docker任务
@Test
public void submitsDocker() {
    string tenantId = "ocs_test";
    Submits submitsDocker = Submits.create()
            .setDockerImage("dockerhub.XXXX.com/demo-slice")
            .setTaskName("demo-slice")
            .addArgs("/10002/res/L2.mp4")
            .addEnv("environment", "dev")
            .addResources(2.0, 2048.0);
 
    Long taskId = JuiceClient.create(“http://your-juice-rest-host", "your-system-id-in-string")
            .setOperations(submitsDocker)
            .handle();
 
    if(null != taskId) {
        System.out.println("submitsDocker, taskId --> " + taskId);
    }
}
</pre>

<pre>
提交一个Commands任务
@Test
public void submitsCommands() {
    string tenantId = "ocs_test";
    Submits submitsCommands = Submits.create()
            .setCommands("/home/app/commands-test/entrypoint.sh")
            .setTaskName("test-commands")
            .addConstraints(Constraints.FIELD.HOSTNAME, "192.168.0.1")           //   Constraints目前支持2种约束模式，hostname/rack_id
            .addConstraints(Constraints.FIELD.HOSTNAME, "192.168.0.2")           //   当选用HOSTNAME模式时,juice会从符合的N个HOSTNAME中选取一个执行任务       
            .addConstraints(Constraints.FIELD.HOSTNAME, "192.168.0.3")           //   当选用RACK_ID模式时,juice会从符合该rack_id的Agent中选取一个执行任务，关于rack_id需要在mesos上设置attribute.
            .addResources(0.5, 256.0);
 
    Long taskId = JuiceClient.create(“http://your-juice-rest-host", "your-system-id-in-string")
            .setOperations(submitsCommands)
            .handle();
 
    if(null != taskId) {
        System.out.println("submitsCommands, taskId --> " + taskId);
    }
}
</pre>


<pre>
查询任务状态
@Test
public void querys() {
    string tenantId = "ocs_test";
    Querys querys = Querys.create()
                .addTask(832419514512333559L);
    List<Task> tasks = JuiceClient.create(“http://your-juice-rest-host", "your-system-id-in-string")
                .setOperations(querys)
                .handle();
     
    if (null != tasks && tasks.size() > 0) {
        System.out.println("query tasks : "
            + tasks.stream().map(Task::toString).collect(Collectors.joining(",", "{\n\t\t", "\n}")));
    }
}
</pre>

<pre>
终止一个正在执行的任务
@Test
public void kills() {
    string tenantId = "ocs_test";
    Kills kills = Kills.create()
            .setTaskId(832419514512333759L);
    TaskKill killed = JuiceClient.create(“http://your-juice-rest-host", "your-system-id-in-string")
                .setOperations(kills)
                .handle();
    if(null != killed) {
        System.out.println("submitsDocker, is killed ? --> " + killed.toString());
    }
}
</pre>

<pre>
同步一个任务的状态
@Test
public void reconciles() {
    string tenantId = "ocs_test";
    Reconciles reconciles = Reconciles.create()
                    .addTask(832419514512333759L);
 
    TaskReconcile taskReconcile = JuiceClient.create(“http://your-juice-rest-host", "your-system-id-in-string")
                    .setOperations(reconciles)
                    .handle();
 
    if(null != taskReconcile) {
        System.out.println("reconciles, taskReconcile --> " + taskReconcile);
    }
}
</pre>
