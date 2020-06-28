# configurator-cli

微服务配置服务 java SDK

**_maven 依赖_**

```
<dependency>
    <artifactId>configurator-cli</artifactId>
    <packaging>jar</packaging>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

**_java code_**

```
Configurator configurator = Configurator.Builder.newBuilder()
    .master("localhost:9090")
    .env("test")
    .group("cbs")
    .project("user-service")
    .version("1.0")
    .build();
System.out.println(configurator.get("redis.host"));
Thread.sleep(3000);
configurator.close();
```
