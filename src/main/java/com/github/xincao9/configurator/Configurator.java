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
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.github.xincao9.configurator.dkv.DkvClient;
import com.github.xincao9.configurator.dkv.DkvClientImpl;
import com.github.xincao9.configurator.dkv.DkvException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private ScheduledExecutorService remoteSyncExecutorService;
    private String path;
    private final Map<String, Object> properties = new ConcurrentHashMap<>();
    private final JavaPropsMapper javaPropsMapper = new JavaPropsMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Integer hashCode;

    private void init() throws ConfiguratorException, DkvException {
        path = String.format("%s/%s/%s/%s/%s", System.getenv("HOME"), env, group, project, version);
        File dir = new File(path);
        try {
            dir.mkdirs();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage());
            throw new ConfiguratorException(String.format("mkdir %s failure", path));
        }
        dkvClient = new DkvClientImpl(master, slaves);
        remoteSyncExecutorService = Executors.newSingleThreadScheduledExecutor((Runnable r) -> new Thread(r, "远程配置同步任务"));
        Runnable r = () -> {
            refresher();
        };
        r.run();
        remoteSyncExecutorService.scheduleAtFixedRate(r, SystemConstant.CONFIGURATOR_REFRESHER_SECONDS, SystemConstant.CONFIGURATOR_REFRESHER_SECONDS, TimeUnit.SECONDS);
    }

    public void refresher() {
        String k = key();
        File file;
        int hashCode = 0;
        try {
            String v = dkvClient.get(k);
            if (StringUtils.isBlank(v)) {
                return;
            }
            hashCode = v.hashCode();
            if (this.hashCode != null && this.hashCode.equals(hashCode)) {
                return;
            }
            file = new File(String.format("%s/%s", path, FILENAME));
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(v);
            }
        } catch (DkvException | IOException e) {
            LOGGER.error(e.getMessage());
            return;
        }
        String data = null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return;
        }
        try {
            if (!StringUtils.isBlank(data)) {
                Map m = objectMapper.readValue(data, Map.class);
                Map<String, Object> props = new HashMap<>();
                javaPropsMapper.writeValue(props, m);
                properties.forEach((s, o) -> {
                    if (!props.containsKey(s)) {
                        properties.remove(s);
                    }
                });
                props.forEach((s, o) -> {
                    properties.put(String.valueOf(s), o);
                });
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        this.hashCode = hashCode;
    }

    /**
     * 构建器
     */
    static class Builder {

        private final Configurator configurator;

        private Builder(Configurator configurator) {
            this.configurator = configurator;
        }

        /**
         * 配置属性
         *
         * @param master dkv master地址
         * @return 构造器
         */
        public Builder master(String master) {
            configurator.master = master;
            return this;
        }

        /**
         * 配置属性
         *
         * @param slaves dkv slaves地址
         * @return 构造器
         */
        public Builder slaves(Set<String> slaves) {
            configurator.slaves = slaves;
            return this;
        }

        /**
         * 配置属性
         *
         * @param env 环境
         * @return 构造器
         */
        public Builder env(String env) {
            configurator.env = env;
            return this;
        }

        /**
         * 配置属性
         *
         * @param group 业务组
         * @return 构造器
         */
        public Builder group(String group) {
            configurator.group = group;
            return this;
        }

        /**
         * 配置属性
         *
         * @param project 项目
         * @return 构造器
         */
        public Builder project(String project) {
            configurator.project = project;
            return this;
        }

        /**
         * 配置属性
         *
         * @param version 版本
         * @return 构造器
         */
        public Builder version(String version) {
            configurator.version = version;
            return this;
        }

        /**
         * 创建构造器
         *
         * @return 构造器
         */
        public static Builder newBuilder() {
            return new Builder(new Configurator());
        }

        /**
         * 构建
         *
         * @return 配置器
         * @throws ConfiguratorException 配置器异常
         * @throws DkvException          dkv异常
         */
        public Configurator build() throws ConfiguratorException, DkvException {
            configurator.init();
            return configurator;
        }
    }

    public String key() {
        return String.format(KEY_FORMAT, env, group, project, version);
    }

    /**
     * 获取属性值
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return properties.get(key);
    }

    /**
     * 获取属性值
     *
     * @param key 键
     * @return 值
     */
    public Byte getByte(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Byte.valueOf((String) value);
    }

    /**
     * 获取属性值
     *
     * @param key 键
     * @return 值
     */
    public Short getShort(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Short.valueOf((String) value);
    }

    /**
     * 获取属性值
     *
     * @param key 键
     * @return 值
     */
    public Integer getInteger(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    /**
     * 获取属性值
     *
     * @param key 键
     * @return 值
     */
    public Long getLong(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    /**
     * 获取属性值
     *
     * @param key 键
     * @return 值
     */
    public Float getFloat(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Float.valueOf(String.valueOf(value));
    }

    /**
     * 获取属性值
     *
     * @param key 键
     * @return 值
     */
    public Double getDouble(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Double.valueOf(String.valueOf(value));
    }

    /**
     * 获取属性值
     *
     * @param key 键
     * @return 值
     */
    public Boolean getBoolean(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Boolean.valueOf(String.valueOf(value));
    }

    /**
     * 获取属性值
     *
     * @param key 键
     * @return 值
     */
    public String getString(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    /**
     * 获取配置属性
     *
     * @return 配置属性
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * 关闭资源
     */
    public void close() {
        remoteSyncExecutorService.shutdown();
    }

    public DkvClient getDkvClient() {
        return dkvClient;
    }

    public Integer getHashCode() {
        return hashCode;
    }
}
