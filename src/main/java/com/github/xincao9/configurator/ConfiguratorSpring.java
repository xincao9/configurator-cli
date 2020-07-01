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
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ConfiguratorSpring {

    private static Configurator configurator;

    public static Configurator getConfigurator (Environment environment) {
        if (configurator != null) {
            return configurator;
        }
        synchronized (ConfiguratorSpring.class) {
            if (configurator != null) {
                return configurator;
            }
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
        }
        return configurator;
    }
}
