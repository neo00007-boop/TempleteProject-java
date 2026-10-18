package com.lib.network;

/**
 * 多域名切换。接口上加 {@code @Headers({Hosts.HOST2})}，请求会被换成 {@link HttpConfig.Builder#host} 里登记的域名。
 */
public interface Hosts {
    String hostKey = "host";

    String host1 = "host1";
    String host2 = "host2";

    String hostHeader1 = hostKey + ":" + host1;
    String hostHeader2 = hostKey + ":" + host2;
}
