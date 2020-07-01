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
