package com.github.xincao9.configurator.dkv;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private String endpoint;

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

    public DkvClientImpl(String address) throws DkvException {
        if (StringUtils.isBlank(address)) {
            throw new DkvException("dkv 地址不能为空");
        }
        if (!StringUtils.startsWith(address, HTTP)) {
            endpoint = String.format("http://%s/kv", address);
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
        Request request = new Request.Builder()
                .url(String.format("%s/%s", endpoint, key))
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            Result result = JSONObject.parseObject(response.body().string(), Result.class);
            if (!OK_STATUS.equals(result.getCode())) {
                throw new DkvException(String.format("期望状态码%d, 实际为 %d", OK_STATUS, result.getCode()));
            }
            return result.getKv().getV();
        } catch (IOException e) {
            throw new DkvException(e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * 设置键值对
     *
     * @param key 键
     * @param value 值
     * @throws DkvException Dkv异常
     */
    @Override
    public void set(String key, String value) throws DkvException {
        JSONObject data = new JSONObject();
        data.put("k", key);
        data.put("v", value);
        RequestBody body = RequestBody.create(data.toJSONString(), JSON);
        Request request = new Request.Builder()
                .url(endpoint)
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
                .url(String.format("%s/%s", endpoint, key))
                .delete()
                .build();
        exec(request);
    }

    private void exec(Request request) throws DkvException {
        try (Response response = client.newCall(request).execute()) {
            if (response == null || response.body() == null) {
                return;
            }
            Result result = JSONObject.parseObject(response.body().string(), Result.class);
            if (!OK_STATUS.equals(result.getCode())) {
                throw new DkvException(String.format("期望状态码%d, 实际为 %d", OK_STATUS, result.getCode()));
            }
        } catch (IOException e) {
            throw new DkvException(e.getMessage());
        }
    }
}
