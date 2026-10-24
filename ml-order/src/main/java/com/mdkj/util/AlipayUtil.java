package com.mdkj.util;

import com.alipay.easysdk.kernel.Config;

/** @author 油桃老师 */
public class AlipayUtil {

    /** 应用ID */
    private static final String APPID = "9021000132690841";
    /** 异步通知接口（下单成功后支付宝回调） */
    private static final String NOTIFY_URL = "http://29e731df.r8.cpolar.cn/api/v1/order/notify";
    /** 支付宝公钥 */
    private static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1Bs8HxaDgG+QecnG0mYs+XQ85SDgxN2xrXxqeFd6s8iqHy/6eIyq9jWE2Ryj4WkHbT0V37HndRC07wrd9P7cs1c8P6Uvv3gEttku1Nj149pw4wUVTdWl+tatw80Q388Q4lIhYwKlez1TggOMLWZUrB4t+3iXXaCSUnMSDMzlhOO6Lv4+uiqletXxS2NpLTNSB0EXHqtv7O4a8oos+7SXuntwTvILHWDSihEl1eVMVznAKz1I/Cwij5h22rJn6LcYc4o2u2gMJlqKQIu57yPOEWfRADTmGChvbFXIePwT5b4P/wsE2OQ0V7HJGqBpp5kL35hNMG1Mvf/a3kLZ+qZ97wIDAQAB";
    /** 应用私钥 */
    private static final String MERCHANT_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCgmAFC2amB+f2Ng5ilkt9jmR+9jCGwkSWocrjQpyPXPpS962uGxde8s3FLz6v44oIUxLT99yZpLyGAsqnA4htxMmiaS2lWMvNUfDVej/iOEXqARbdeaq2kubdE6ThSmdtayywt4L8GsRf8vN43/Ab2se8D4Du3JBTFM3oc3J6V5bgo7bOoL2vc1hF1Mnq4ZdfRZHgV+uqGY4Th46qj/Zrh3/t7I8EwhYbV7t5Rk4KU5LhByTDRuyDWpXdAKCCzJ+8acLZmpCxM0BogLpcpH4YsyAoTFdqrvjuHWhbbQDEooBwmtexx7tKDP4MLGiqsgtWjpiEKXn/vzQL94y+o3K/rAgMBAAECggEAKbayBSpChFdqC9sVyFQ5IEbC3tpKWfGpxXJfHRt9DMjdm+NNn8k9+27ky9tagSBcx4Y8uQb8fC/Y1PQTcd2JYULFtuZFqzU6zUQiHLjqyAQ0PKNfOPAewH3Y49ZsYdBDEPdbN2N8WwGfCHAKjKIzMNsRCYGyISUHHB395wdLTgXd+eCv5Q8dTuatJnKoZukyGpfgi2iVUVVhDbBvEx4H10Ry6LKqo5AlnML9yPfDBQYy5xANAXiCnhUlYF4JFHVHqvk5/Pe9L0DjZNgiMPGiElnbURUpwFXLkp6D2U97Gm+OjnqAwZbFK4J1sOPYLsCO9d8lvKpNLFfbJF4mTbG4mQKBgQDqX8QQHMsDfKmpyxnovZZDonXQdbeb4pMYri0fgGV6giKW5oR1kBqnGlAY5kALH+DxkS0DEjwT2IsbNlVMHJzuEPnoRAiYzwU8LRHsBr0SmilVhxOj8J9P2GiNnQyMJs7ml7E///Z7rCo5kdtVh9FyIrTInvbLqunjLe1phtmnvQKBgQCvaXKjl+tMHj64lalc6lmKpvCIU5/6eIXfATTeyjBckYr7NCgj93p7M7kzWeNikmWK2Qya9vNP2EEybylimqDHIpCcoCctEH3gEvh4aIqt3LNX0hDRkxw3EKWsy+tdCMFMclqhSQcFON5wTDZR2TQjNS1ihznHctB0R7UCF/c8xwKBgCXDyJPQD+Y/8Bar40DdS5kaVGGCllNH7uvLJPoudiuHTIuEGVPYJHNIih94+ljQii5idh5IkeWgvsljt9rLwX2avqnuTHRS50UK/hhJ/a1BqedeHIwN+Z2kATTQWXU7y5rcVnM5iz3iP+uG1RCzEcySSw8IjjvDDmzMubSHRgsBAoGAH99FGTyNYHNOajOnZl6ejnbu1ZZ8RwKHMJIDjUenqeo4GpTLhl19P82SJSZ8DnkTQjtV8wJUC3n7vrI8DtKRzB0XsZ3YAHQyqvhHdXlpWVX0sExXCf107vxzXgZfabQhi0Zr+UpPoD9JC5aNeCnDM3KCIJj5gqf2AGrXFN5N6hkCgYAn8vGOq7AzF1kiOcOVA93iFQ8BGbm6qCG29QZxjn8NasvvQb/0CHS1QikS0+Vl5oF955N2mgYVG89Ni3bl3UqwR8qtOKFExLftOOoL9iwrfV+Oi+94knU0QtPP2RBCp+i+Ha3rjoCnOpF2WHVSv6gPzDWrmNmaK5KVSPqxQnxxBw==";
    /** 单例的Alipay配置对象 */
    private static volatile Config config;
    /** 单例对外方法 */
    public static Config getConfig() {
        if (config == null) {
            synchronized (AlipayUtil.class) {
                if (config == null) {
                    config = new Config();
                    config.protocol = "https";
                    config.gatewayHost = "openapi-sandbox.dl.alipaydev.com";
                    config.signType = "RSA2";
                    config.ignoreSSL = true;
                    config.appId = APPID;
                    config.alipayPublicKey = ALIPAY_PUBLIC_KEY;
                    config.merchantPrivateKey = MERCHANT_PRIVATE_KEY;
                    config.notifyUrl = NOTIFY_URL;
                }
            }
        }
        return config;
    }
}