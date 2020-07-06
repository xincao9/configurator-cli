# configurator-cli

配置器的特点:

1. 完整的配置管理功能
2. 多环境，多业务组，多服务，多版本 的配置分类

![architectures](https://raw.githubusercontent.com/xincao9/configurator/master/configurator.png)

## 安装中间件

**安装 [dkv](https://github.com/xincao9/dkv)**

```
docker pull xincao9/dkv
docker run -d -p 9090:9090 -p 6380:6380 dkv:latest
```

**创建服务配置**

>接口
```
curl -X PUT -H 'content-type:application/json' 'http://localhost:9090/kv' -d '{"k":"configurator|TEST|BASE|USER-SERVICE|v1.0", "v":"{\"redis\":{\"host\":\"localhost\",\"port\":\"6379\"}}"}'
```

>推荐使用 [configurator-ui](https://github.com/xincao9/configurator/tree/master/api) 系统管理配置

## 如何使用 微服务配置服务 java SDK

**_maven 依赖_**

```xml
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

```properties
configurator.dkv.master=localhost:9090
configurator.dkv.slaves=
configurator.env=TEST
configurator.group=BASE
configurator.project=USER-SERVICE
configurator.version=v1.0
```

**_spring boot java_**

```java
package com.github.xincao9.configurator.cli.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Value("${redis.host}")
    private String host;
    @Value("${redis.port}")
    private Integer port;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return (String... args) -> {
            for (int no = 0; no < 100; no++) {
                LOGGER.info("redis.host = {}, redis.port = {}", host, port);
            }
        };
    }
}
```
