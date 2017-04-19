## Request Header

| name          | type          | is Required  | Description    |  
| ------------- |:-------------:| ------------:| --------------:| 
| X-Tenant-Id   | String        | Y            | 请求方系统标示号 | 

#### 1.提交一个任务
URL:/v1/tasks       
Method : POST

## RequestBody

| name          | type          | is Required             | Description    |  
| ------------- |:-------------:| -----------------------:| --------------:| 
| taskName      | String        | Y                       | 任务名称        |
| callbackUrl   | String        | N                       | 任务回调URL     |
| resources     | Resources     | N                       | 任务所需硬件资源 |
| container     | Container     | N(运行Container任务时非空)| Container任务  |
| commands      | String        | N(运行Commands任务时非空) | Commands任务   |
| env           | Environment   | N                       | 环境变量        |
| args          | List<String>  | N(Container任务时可传字段)| 用户参数        |
| constraints   | Constraints   | N                       | 约束           |

ps: type中的对象定义参见[对象定义说明](object.md)
<pre>
example run container:
{  
   "callbackUrl":"http://www.XXXXXXXX.com/v5/tasks/callback",	    
   "taskName":"demo-container",									        
   "env":{"name":"environment","value":"dev"},   				
    "args":["this is a test"],                     			
    "container":{       		
      	"docker":{
        	"image":"dockerhub.XXXX.com/demo-slice"        	
      	},
    	"type":"DOCKER"
    }
}
</pre>
<pre>
example run commands:
{  
    "callbackUrl":"http://www.XXXXXXXX.com/v5/tasks/callback",
	"taskName":"demo-commands",									
	"env":{"name":"environment","value":"dev"},   				
  	"commands":"/home/app/entrypoint.sh"						
}
</pre>

## ResponseBody

| name          | type          | is Required             | Description    |  
| ------------- |:-------------:| -----------------------:| --------------:| 
| taskId        | Long          | Y                       | Juice任务ID     |

<pre>
response ok, httpcode 200:
{  
	"status": 0,
	"message":"OK",
	"data" :{
		"taskId": 806769905152840693
	}						
}

response failed, httpcode 400/500:
{
	"status": -2127552257,
	"message":"object not null error"
}
</pre>

#### 2.(批量)查询任务状态
URL:/v1/tasks?taskIds={ids}       
Method : GET

## RequestParams

| name          | type          | is Required             | Description               |  
| ------------- |:-------------:| -----------------------:| -------------------------:| 
| taskIds       | String        | Y                       | Juice任务IDs(Id间以逗号分割)|

## ResponseBody

| name          | type          | is Required             | Description               |  
| ------------- |:-------------:| -----------------------:| -------------------------:| 
| taskResults   | List<Task>    | Y                       | 任务信息列表                |

<pre>
response ok, httpcode 200:
{  
"status": 0,
	"message":"OK",
	"data" :{
		[{
			"taskId": 806769905152840693,
			"taskName": "test-docker",
			"tenantId": "10002",
			"dockerImage": "dockerhub.XXXX.com/juice-test",
			"taskStatus": 2,									//	参考taskStatus说明
			"message": "Container exited with status 0",
			"callbackAt": "Dec 8, 2016 4:03:08 PM",
			"callbackUrl": "http://localhost:9999/v1/callback",
			"submitAt": "Dec 8, 2016 3:58:29 PM"
		},
		{
			"taskId": 806769905152840695,
			"taskName": "test-commands",
			"tenantId": "10002",
			"commands": "/home/app/entrypoint.sh",
			"taskStatus": 3,
			"message": "Container exited with status 0",
			"callbackAt": "Dec 8, 2016 4:04:08 PM",
			"callbackUrl": "http://localhost:9999/v1/callback",
			"submitAt": "Dec 8, 2016 3:54:29 PM"
		}]
	}						
}

response failed, httpcode 400/500:
{
	"status": -2127552257,
	"message":"object not null error"
}
</pre>

#### 3.终止一个任务

URL:/v1/tasks/kill?taskId={id}       
Method : POST

## RequestParams

| name          | type          | is Required             | Description               |  
| ------------- |:-------------:| -----------------------:| -------------------------:| 
| taskId        | Long          | Y                       | Juice任务ID                |

## ResponseBody

| name          | type          | is Required             | Description               |  
| ------------- |:-------------:| -----------------------:| -------------------------:| 
| submitKill    | Boolean       | Y                       | 是否提交删除                |
| resultMessage | String        | Y                       | 描述信息                    |
| currentStatus | byte          | Y                       | 当前状态                    | 
|               |               |                         | NOT_START(-1),             |
|               |               |                         | STAGING(0),                |
|               |               |                         | RUNNING(1),                |
|               |               |                         | FINISHED(2),               |
|               |               |                         | FAILED(3),                 |
|               |               |                         | LOST(4),                   |
|               |               |                         | ERROR(5),                  |
|               |               |                         | KILLED(6),                 |
|               |               |                         | UNREACHABLE(7),            |
|               |               |                         | DROPPED(8),                |
|               |               |                         | GONE(9),                   |
|               |               |                         | GONE_BY_OPERATOR(10),      |
|               |               |                         | UNKNOWN(11),               |
|               |               |                         | EXPIRED(12);               |

<pre>
response ok, httpcode 200:
{  
	"status": 0,
	"message":"OK",
	"data" :{
		"submitKill":true,
		"resultMessage":"task is killed",
		"currentStatus":6
	}					
}

response failed, httpcode 400/500:
{
	"status": -2127552257,
	"message":"object not null error"
}
</pre>