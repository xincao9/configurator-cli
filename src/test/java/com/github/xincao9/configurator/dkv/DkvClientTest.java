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
package com.github.xincao9.configurator.dkv;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class DkvClientTest {

    private DkvClient dkvClient;
    private final AtomicInteger counter = new AtomicInteger(0);

    @Setup
    public void setUp() throws Throwable {
        dkvClient = new DkvClientImpl("localhost:9090", null);
    }

    @TearDown
    public void tearDown() throws Throwable {
    }

    @Benchmark
    public void cmd() throws DkvException {
        String value = RandomStringUtils.randomAscii(128);
        String no = String.valueOf(counter.incrementAndGet());
        dkvClient.set(no, value);
        System.out.println(dkvClient.get(no));
        dkvClient.delete(no);
    }

    @Test
    public void testMethod() throws Throwable {
        Options opt = new OptionsBuilder()
                .include(getClass().getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
