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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConfiguratorBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfiguratorBeanPostProcessor.class);
    private Environment environment;
    private Configurator configurator;
    private List<Pair<String, Pair<Object, Field>>> keyBeans = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void initMethod() {
        try {
            Set<String> slaves = new HashSet();
            String slavesStr = environment.getProperty(SystemConstant.DKV_SLAVES);
            if (StringUtils.isNoneBlank(slavesStr)) {
                slaves = new HashSet(Arrays.asList(StringUtils.split(slavesStr, ",")));
            }
            configurator = Configurator.Builder.newBuilder()
                .master(environment.getProperty(SystemConstant.DKV_MASTER))
                .slaves(slaves)
                .env(environment.getProperty(SystemConstant.ENV))
                .group(environment.getProperty(SystemConstant.GROUP))
                .project(environment.getProperty(SystemConstant.PROJECT))
                .version(environment.getProperty(SystemConstant.VERSION))
                .build();
        } catch (ConfiguratorException | DkvException e) {
            throw new RuntimeException(e);
        }
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (keyBeans.isEmpty()) {
                        return;
                    }
                    keyBeans.forEach((pair) -> {
                        String key = pair.getO1();
                        Object bean = pair.getO2().getO1();
                        Field field = pair.getO2().getO2();
                        if (field == null) {
                            return;
                        }
                        try {
                            Object value = configurator.get(key);
                            if (field.getType() == Short.class) {
                                value = Short.parseShort((String) value);
                            } else if (field.getType() == Integer.class) {
                                value = Integer.parseInt((String) value);
                            } else if (field.getType() == Long.class) {
                                value = Long.parseLong((String) value);
                            } else if (field.getType() == Float.class) {
                                value = Float.parseFloat((String) value);
                            } else if (field.getType() == Double.class) {
                                value = Double.parseDouble((String) value);
                            } else if (field.getType() == Boolean.class) {
                                value = Boolean.parseBoolean((String) value);
                            }
                            if (value != null) {
                                field.setAccessible(true);
                                field.set(bean, value);
                                LOGGER.warn("key = {}, bean = {}, field = {}", key, bean.getClass().getSimpleName(), field.getName());
                            }
                        } catch (Throwable e) {
                            LOGGER.error(e.getMessage());
                        }
                    });
                }
            }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class clazz = ClassUtils.getUserClass(bean); // the original class for CGLIB-generated classes, consider ClassUtils.getUserClass
        Field[] fields = clazz.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                if (field.isAnnotationPresent(Value.class)) {
                    Value value = field.getAnnotation(Value.class);
                    String key = value.value();
                    if (StringUtils.isBlank(key)) {
                        continue;
                    }
                    key = StringUtils.substringBetween(key, "${", "}");
                    if (StringUtils.isBlank(key)) {
                        continue;
                    }
                    keyBeans.add(new Pair(key, new Pair<>(bean, field)));
                }
            }
        }
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}