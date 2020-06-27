package com.github.xincao9.configurator;

import com.github.xincao9.configurator.dkv.DkvClient;
import com.github.xincao9.configurator.dkv.DkvClientImpl;

import java.util.Set;

/**
 * 配置器
 *
 * @author xincao9@gmail.com
 */
public class Configurator {

    private String master;
    private Set<String> slaves;
    private String env;
    private String group;
    private String project;
    private String version;
    private DkvClient dkvClient;

    private void init() throws Throwable {
        this.dkvClient = new DkvClientImpl(master);
        // 异步任务，定时同步配置从远程到本地
    }

    static class Builder {

        private Configurator configurator;

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
}
