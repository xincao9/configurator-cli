/*
 * Copyright 2020 Personal.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xincao9.configurator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xincao9.configurator.dkv.DkvClient;
import java.util.HashMap;
import java.util.Map;
import org.junit.*;

/**
 * @author xincao9@gmail.com
 */
public class ConfiguratorTest {

    private static final String MASTER = "localhost:9090";
    private static final String ENV = "TEST";
    private static final String GROUP = "BASE";
    private static final String PROJECT = "USER-SERVICE";
    private static final String VERSION = "v1.0";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConfiguratorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * @throws java.lang.Throwable
     */
    @Test
    public void testGet() throws Throwable {
        Configurator configurator = Configurator.Builder.newBuilder()
            .master(MASTER)
            .env(ENV)
            .group(GROUP)
            .project(PROJECT)
            .version(VERSION)
            .build();
        DkvClient dkvClient = configurator.getDkvClient();
        Map<String, Object> config = new HashMap();
        Map<String, Object> redis = new HashMap();
        config.put("redis", redis);
        redis.put("host", "127.0.0.1");
        redis.put("port", 6379);
        dkvClient.set(configurator.key(), objectMapper.writeValueAsString(config));
        configurator.refresher();
        System.out.println(configurator.get("redis.host"));
        System.out.println(configurator.get("redis.port"));
        dkvClient.delete(configurator.key());
        configurator.close();
    }

}
