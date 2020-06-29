# configurator-cli

微服务配置服务 java SDK

**_maven 依赖_**

```
<dependency>
   <groupId>com.github.xincao9</groupId>
   <artifactId>configurator-cli</artifactId>
   <version>1.0-SNAPSHOT</version>
</dependency>
```

**_java code_**

```
Configurator configurator = Configurator.Builder.newBuilder()
    .master("localhost:9090")
    .env("TEST")
    .group("BASE")
    .project("USER-SERVICE")
    .version("v1.0")
    .build();
System.out.println(configurator.getString("redis.host"));
System.out.println(configurator.getInt("redis.port"));
Thread.sleep(3000);
configurator.close();
```

**_spring boot configurator_**

```
configurator.dkv.master=localhost:9090
configurator.dkv.slaves=
configurator.env=TEST
configurator.group=BASE
configurator.project=USER-SERVICE
configurator.version=v1.0
```
