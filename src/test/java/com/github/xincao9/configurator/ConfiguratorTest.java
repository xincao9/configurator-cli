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

import org.junit.*;

/**
 * @author xincao9@gmail.com
 */
public class ConfiguratorTest {

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
            .master("localhost:9090")
            .env("test")
            .group("cbs")
            .project("user-service")
            .version("1.0")
            .build();
        System.out.println(configurator.get("redis.host"));
        Thread.sleep(3000);
        configurator.close();
    }

}
