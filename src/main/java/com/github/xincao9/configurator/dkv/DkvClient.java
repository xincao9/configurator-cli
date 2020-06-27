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

/**
 * DKV 客户端
 *
 * @author xincao9@gmail.com
 */
public interface DkvClient {

    /**
     * 获取键值
     *
     * @param key 键
     * @return 值
     * @throws DkvException Dkv异常
     */
    String get(String key) throws DkvException;

    /**
     * 设置键值对
     *
     * @param key   键
     * @param value 值
     * @throws DkvException Dkv异常
     */
    void set(String key, String value) throws DkvException;

    /**
     * 删除键值
     *
     * @param key 键
     * @throws DkvException Dkv异常
     */
    void delete(String key) throws DkvException;
}
