package com.lib.network;

/**
 * 多域名切换。接口上加 {@code @Headers({Hosts.HOST2})}，请求会被换成 {@link HttpConfig.Builder#host} 里登记的域名。
 */
public interface Hosts {
    String HEADER = "host";

    String KEY1 = "host1";
    String KEY2 = "host2";

    String HOST1 = HEADER + ":" + KEY1;
    String HOST2 = HEADER + ":" + KEY2;
}
