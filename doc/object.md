<pre>
class Resources {
    String role;
    int cpu;
    int mem;
}
</pre>

<pre>
class Environment {
    String name;
    String value;
}
</pre>

<pre>
class Constraints {
    String field;           //  "RACK_ID"/"HOSTNAME"
    Set<String> values;
}
</pre>


<pre>
//  目前Container只支持运行Docker模式
class Container {
    Docker docker;
    String type;            //  "DOCKER"/"MESOS"
    List<Volume> volumes;
 
    class Volume {
        String containerPath;
        String hostPath;
        String dvo;         //  "RO"/"RW"
    }
  
    class Docker {
        String image;
        boolean forcePullImage;
        boolean privileged;
        String net;                     //  "BRIDGE"/"HOST"/"NONE"/"USER"
        List<Parameter> parameters;
        List<PortMapping> portMappings;
 
        class PortMapping {
            int containerPort;
            int hostPort;
            String protocol;
        }
        class Parameter {
            String key;
            String value;
        }
    }
}
</pre>