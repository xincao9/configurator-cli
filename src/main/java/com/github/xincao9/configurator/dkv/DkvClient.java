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
