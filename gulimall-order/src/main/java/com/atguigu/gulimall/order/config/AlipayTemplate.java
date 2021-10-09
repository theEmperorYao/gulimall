package com.atguigu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2021000118627644";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDATx/6qHOBdSuuEcJ8/hIS/kjJd4aKDk9AZ8Uq0IM1lNize3vpn+O54bwfMtQKHL18ASmXiHWIuhR+pYSRGrZ9hz+dUARj6Id8348CkPduTXdZoE/JsGdKnVSneiFkyJCsHH6HV9uMknGxDCDEvRHicxmDzdGkGO+UNIv6mQQqpjLJ/o4Lcrgkfupi/5NYyx3Ly3KuAzAaAop/+uiUIjvVSAEsFjFuOrRqJhp0/vQJzKfS4+Mu8g5P3o6EgRsbMNGDtETqfdpYjkmk+l7yeAQKpAJ1A5VY9MQNZSp26FSEmfjwD3Uu9hPOQ3N/f6MCZbztKXXaO5T1p9QyBLqs9J+5AgMBAAECggEBAIUcf8MtpD3gj6p+SoPgDTyuXE1veWXH/91Rtq0rBRcqf1DE0YFllehNMrzZnuGdAR97YoGsB8brHlKHWRpjRhvfaQ4Hm/JXuqiZydB57XqhDlKwX2n4u5ZvDnCaXdD37wPi+UXPb31+xElLXscvI9j92JRd+5U9WV/Ze2arUbH+gpqAdZYv5xF3MlACAWMEKun95Am9NwpSzPNDriIFg/NfJhvYOTtHxoUHCCUUlF07aDUkk69B7u4Z+cx9L4MLv4iqr34f0n7RXN3oy9LF/Lf1vzSDyJTOFQVHxDeEr5tmhsblxIIpm3Zkwud8n928uFPYn/RCNpYwMOB7I6wVR7ECgYEA6mcOniipkfTWVdY4rXZYizirlnGEs1mPjDuh9zZRzhmPHZYoy+xZOYERvCQZ9WKbyqcsiVrKMLjC/FHDL8+bKbr5CwZDcbi2bJkGj1v9qj8hQElkDbVICdUUSb5n2vOLF8nQb+tpOk6+eILwCja7E1wY9eEWdJLbqC9ZozVs/hUCgYEA0gcxVPXnF2dz9njxwdLF1c2f61zUQxcnnm/GdKweK/uqqMx62Nf28tgpzQs5G0nhDZCu0bBprW35RuP4y9208F9UN4W6bMkbugoTNks5hz3E9boKCVq8cfdG/8ZIk52tChnVVb/8WnRlJnBnqwWHyTeT71zVift5c3rX/UquqBUCgYAcPb5tH8kOZMlOHjlF7PROEc3ammK6mRfKdYTWHB1PD5VGo0jXCOrXGs3gNRlLj03l5K5g/idOJ3SWoeM6Z3DyXiyTP3Xwxah0/RrlOtgdIu4GqND9NdIwHRQMrnwkzSL9AruyQFK1S7d3HY9+K4nukzIOrcfe07LTWKEcINu44QKBgC9NP2cLGsjCm/NeudQVTEkizmmLtqKFjH66Xc/ZE9KEE6OAqgo8WXnIBWIQrQPgV7deZoEDYkmVAxdcMKR5gk7AqY73p0zo3j3AFyObPZGf78QH6krBDL/+GRdx6NaWJGqp9sohOwyEOmO13SM/U52VztOR7OXupSPE/vAk6cEhAoGADsoDCh9udkVYDfLXKbmv9GBSxkhwtMffvwokZGLXSN5KRscsF3yry9U9jTgrPYAK56bEbU6GmqjV5szJJIe+U1NZyvhYdSDAmzmXT2d8udJtwjz8aGJG4s85nu+CdVSlA+HdfjtT21uDXWnGrh6Fvde/dqEnDQnrzRTAXZdaYws=";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA08m9caSLd0NPJTplEKji/iuZOwwPuhlZqi3u8frHFPmkpLShzrRdM5/EyN7NWH5jp7RdHQ9K8MyTDd635hpRbXqOZP3Zy0exUy6WVJweEkFtjZnxFY6kk214eS79AJThli03K26gHkZ3yCHmNhzx3DVSwNxD1UjJMs3g9BQ6LYgP6Q8E7HLO2qRYObGEVr2Ob6mdyaOwvtqjubNzqTuJhnKxexjMEYnPzGfimj2Gmf3OzQw0Vxq1b6s/S7pkXg1AuJzXmcpitBlA8F/b1rI8XcGlywBlhsbU4CVcWJb+36KO6z4cYXThOn9Y07C6/+iAHpcpsBdkWH2znP/MK6yO+wIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "http://tdgi8aexw0.51xd.pub/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url;

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    private String timeOut="30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"timeout_express\":\""+timeOut+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);

        return result;

    }
}
