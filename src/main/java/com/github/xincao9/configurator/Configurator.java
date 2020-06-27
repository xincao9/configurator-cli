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

import com.github.xincao9.configurator.dkv.DkvClient;
import com.github.xincao9.configurator.dkv.DkvClientImpl;
import com.github.xincao9.configurator.dkv.DkvException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置器
 *
 * @author xincao9@gmail.com
 */
public class Configurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configurator.class);
    private static final String KEY_FORMAT = "configurator|%s|%s|%s|%s";
    private static final String FILENAME = "application.json";

    private String master;
    private Set<String> slaves;
    private String env;
    private String group;
    private String project;
    private String version;
    private DkvClient dkvClient;
    private ScheduledExecutorService scheduledExecutorService;
    private String path;

    private void init() throws ConfiguratorException, DkvException {
        path = String.format("%s/%s/%s/%s/%s", System.getenv("HOME"), env, group, project, version);
        File dir = new File(path);
        dir.mkdirs();
        dkvClient = new DkvClientImpl(master);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor((Runnable r) -> new Thread(r, "远程配置同步任务"));
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            String k = key();
            try {
                String v = dkvClient.get(k);
                if (StringUtils.isNoneBlank(v)) {
                    File file = new File(String.format("%s/%s", path, FILENAME));
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(v);
                    }
                }
            } catch (DkvException | IOException e) {
                LOGGER.error(e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    static class Builder {

        private final Configurator configurator;

        private Builder(Configurator configurator) {
            this.configurator = configurator;
        }

        public Builder master(String master) {
            configurator.master = master;
            return this;
        }

        public Builder slaves(Set<String> slaves) {
            configurator.slaves = slaves;
            return this;
        }

        public Builder env(String env) {
            configurator.env = env;
            return this;
        }

        public Builder group(String group) {
            configurator.group = group;
            return this;
        }

        public Builder project(String project) {
            configurator.project = project;
            return this;
        }

        public Builder version(String version) {
            configurator.version = version;
            return this;
        }

        public static Builder newBuilder() {
            return new Builder(new Configurator());
        }

        public Configurator build() throws Throwable {
            configurator.init();
            return configurator;
        }
    }

    public String key() {
        return String.format(KEY_FORMAT, env, group, project, version);
    }
}
