/*
 * Copyright 2020 xincao9@gmail.com.
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

import com.github.xincao9.configurator.dkv.DkvException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;

/**
 * configurator 从远程加载配置
 *
 * @author xincao9@gmail.com
 */
public class ConfiguratorPropertySourceLoader implements PropertySourceLoader, EnvironmentAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfiguratorPropertySourceLoader.class);

    private static final String EXT = "configurator";
    private static final String DKV_MASTER = "configurator.dkv.master";
    private static final String DKV_SLAVES = "configurator.dkv.slaves";
    private static final String ENV = "configurator.env";
    private static final String GROUP = "configurator.group";
    private static final String PROJECT = "configurator.project";
    private static final String VERSION = "configurator.version";
    private Configurator configurator;
    private Environment environment;

    public ConfiguratorPropertySourceLoader () {
        LOGGER.info("开始加载 {} ", EXT);
    }

    @Override
    public String[] getFileExtensions() {
        return new String[]{EXT};
    }

    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        LOGGER.info("加载 {} 中", EXT);
        try {
            Set<String> slaves = new HashSet();
            String slavesStr = environment.getProperty(DKV_SLAVES);
            if (StringUtils.isNoneBlank(slavesStr)) {
                slaves = new HashSet(Arrays.asList(StringUtils.split(slavesStr, ",")));
            }
            configurator = Configurator.Builder.newBuilder()
                .master(environment.getProperty(DKV_MASTER))
                .slaves(slaves)
                .env(environment.getProperty(ENV))
                .group(environment.getProperty(GROUP))
                .project(environment.getProperty(PROJECT))
                .version(environment.getProperty(VERSION))
                .build();
        } catch (ConfiguratorException | DkvException e) {
            throw new IOException(e);
        }
        return Collections.singletonList(new MapPropertySource(name, configurator.getProperties()));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
