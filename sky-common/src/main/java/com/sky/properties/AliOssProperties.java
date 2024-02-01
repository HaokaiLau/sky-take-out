package com.sky.properties;

import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties {

    private String endpoint;
//    private String accessKeyId;
//    private String accessKeySecret;
    private String bucketName;
    private EnvironmentVariableCredentialsProvider credentialsProvider;
    {
        try {
            credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
