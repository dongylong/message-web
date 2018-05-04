package com.noriental.messageweb.message.vo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by dongyl on 1/20/17.
 */
@Configuration
@PropertySource(value="classpath:config.properties")
public class Config {

    @Value("${server.monitor.host}")
    public  String server_monitor_host;

    @Value("${server.exists.tag}")
    public  String server_exists_tag;
    @Value("${server.exists.ip}")
    public  String server_exists_ip;

    @Value("${app_key}")
    public String appKey;
    @Value("${token_url}")
    public String tokenUrl;
    @Value("${app_id}")
    public String appId;
    @Value("${app_secret}")
    public String appSecret;
    @Value("${send_msg_url}")
    public String sendMSGUrl;
}
