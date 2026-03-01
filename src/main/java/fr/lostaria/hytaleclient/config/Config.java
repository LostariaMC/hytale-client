package fr.lostaria.hytaleclient.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Config {

    private boolean local;
    private String serverId;
    private String authApiUrl;
    private String serverManagerApiUrl;
    private String pubsubApiUrl;
    private String pathDeviceToken;
}
