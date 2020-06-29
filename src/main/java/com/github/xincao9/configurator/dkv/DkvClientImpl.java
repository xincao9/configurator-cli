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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DKV 客户端
 *
 * @author xincao9@gmail.com
 */
public class DkvClientImpl implements DkvClient {

    private final OkHttpClient client = new OkHttpClient();
    private static final String HTTP = "http";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Integer OK_STATUS = 200;
    private String masterEndpoint;
    private List<String> endpoints;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicLong counter = new AtomicLong(0);

    static class KV {

        private String k;
        private String v;

        public String getK() {
            return k;
        }

        public void setK(String k) {
            this.k = k;
        }

        public String getV() {
            return v;
        }

        public void setV(String v) {
            this.v = v;
        }

    }

    static class Result {

        private Integer code;
        private String message;
        private KV kv;

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public KV getKv() {
            return kv;
        }

        public void setKv(KV kv) {
            this.kv = kv;
        }
    }

    /**
     * 构造器
     *
     * @param master 主地址
     * @param slaves 从地址
     * @throws DkvException Dkv异常
     */
    public DkvClientImpl(String master, Set<String> slaves) throws DkvException {
        if (StringUtils.isBlank(master)) {
            throw new DkvException("dkv 地址不能为空");
        }
        if (!StringUtils.startsWith(master, HTTP)) {
            masterEndpoint = String.format("http://%s/kv", master);
        }
        endpoints = new ArrayList();
        endpoints.add(masterEndpoint);
        if (slaves != null && !slaves.isEmpty()) {
            for (String slave : slaves) {
                if (!StringUtils.startsWith(slave, HTTP)) {
                    slave = String.format("http://%s/kv", master);
                }
                endpoints.add(slave);
            }
        }
    }

    /**
     * 获取键值
     *
     * @param key 键
     * @return 值
     * @throws DkvException Dkv异常
     */
    @Override
    public String get(String key) throws DkvException {
        String endpoint = balancer();
        Request request = new Request.Builder()
            .url(String.format("%s/%s", endpoint, key))
            .build();
        try (Response response = client.newCall(request).execute()) {
            Result result = objectMapper.readValue(response.body().string(), Result.class);
            if (!OK_STATUS.equals(result.getCode())) {
                throw new DkvException(String.format("期望状态码%d, 实际为 %d", OK_STATUS, result.getCode()));
            }
            return result.getKv().getV();
        } catch (IOException e) {
            throw new DkvException(e.getMessage());
        }
    }

    /**
     * 轮训负载均衡器
     *
     * @return 端点
     * @throws DkvException Dkv异常
     */
    private String balancer () throws DkvException {
        if (endpoints != null) {
            return endpoints.get((int)(counter.incrementAndGet() % endpoints.size()));
        }
        throw new DkvException("endpoints is empty");
    }

    /**
     * 设置键值对
     *
     * @param key   键
     * @param value 值
     * @throws DkvException Dkv异常
     */
    @Override
    public void set(String key, String value) throws DkvException {
        String data;
        try {
            Map<String, String> map = new HashMap();
            map.put("k", key);
            map.put("v", value);
            data = objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new DkvException(e.getMessage());
        }
        RequestBody body = RequestBody.create(data, JSON);
        Request request = new Request.Builder()
            .url(masterEndpoint)
            .put(body)
            .build();
        exec(request);
    }

    /**
     * 删除键值
     *
     * @param key 键
     * @throws DkvException Dkv异常
     */
    @Override
    public void delete(String key) throws DkvException {
        Request request = new Request.Builder()
            .url(String.format("%s/%s", masterEndpoint, key))
            .delete()
            .build();
        exec(request);
    }

    private void exec(Request request) throws DkvException {
        try (Response response = client.newCall(request).execute()) {
            if (response == null || response.body() == null) {
                return;
            }
            Result result = objectMapper.readValue(response.body().string(), Result.class);
            if (!OK_STATUS.equals(result.getCode())) {
                throw new DkvException(String.format("期望状态码%d, 实际为 %d", OK_STATUS, result.getCode()));
            }
        } catch (IOException e) {
            throw new DkvException(e.getMessage());
        }
    }
}
