package com.noriental.messageweb.message.service.impl;

import com.noriental.messageweb.message.service.ChuangCacheMessageService;
import com.noriental.messageweb.message.vo.RequestChuangCacheMessage;
import com.noriental.validate.bean.CommonDes;
import com.noriental.validate.exception.BizLayerException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;

@Service("message.chuangcacheMessageService")
public class ChuangCacheMessageServiceImpl implements ChuangCacheMessageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource
    private com.noriental.messageweb.message.vo.Config config; //引用统一的参数配置类

    @Override
    public CommonDes sendChuangCacheMessage(RequestChuangCacheMessage request) throws BizLayerException {
        String appKey = config.appKey;

        String mobile = request.getMobile();
        String content = request.getContent();
        String getTokenData = getToken();
        JSONObject tokenJson = JSONObject.fromObject(getTokenData);
        int status = tokenJson.getInt("status");
        String info = tokenJson.getString("info");
        String access_token = "";
        if (status == 1) {
            JSONObject data = tokenJson.getJSONObject("data");
            access_token = data.getString("access_token");
            int expires_in = data.getInt("expires_in");//access_token的生命周期，单位是秒数。过期之后要重新获取
            LOGGER.info("access_token:" + access_token + ",expires_in:" + expires_in);
        }

        String sendSmsResult = sendSms(appKey, access_token, mobile, content);
        LOGGER.info("sendSmsResult::" + sendSmsResult);//sendSmsResult::{"code":1000,"sendid":"2017090516580138555928044","msg":"短信提交成功"}
        JSONObject sendSmsResultJson = JSONObject.fromObject(sendSmsResult);

        int code = sendSmsResultJson.getInt("code");
        if(1000==code){
            return new CommonDes();
        }else{
            LOGGER.error("创世云短信服务异常");
            throw new BizLayerException("短信服务异常",null);
        }
    }


    private String getToken() {
        String tokenUrl = config.tokenUrl;
        String appId = config.appId;
        String appSecret = config.appSecret;
        try {
            URL url = new URL(tokenUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            JSONObject obj = new JSONObject();
            obj.put("appid", appId);//填写appkey (客服人员会给出)
            obj.put("appsecret", appSecret);//填写appsecret(客服人员会给出)
            obj.put("grant_type", "client_credentials");
            out.writeBytes(obj.toString());
            out.flush();
            out.close();

            InputStream inStream = conn.getInputStream();
            return new String(readInputStream(inStream), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String sendSms(String app_key, String access_token, String mobile, String content) {
        OutputStreamWriter out;
        try {
            String sendMSGUrl = config.sendMSGUrl;
            URL url = new URL(sendMSGUrl);//"http://sms.chuangcache.com/api/sms/ordinary"
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject obj = new JSONObject();
            obj.put("access_token", access_token);
            obj.put("app_key", app_key);
            obj.put("mobile", mobile);//手机号码
            obj.put("content", content);//发送内容
            obj.put("time", System.currentTimeMillis() + "");

            out = new OutputStreamWriter(conn.getOutputStream());
            out.write(obj.toString());
            out.flush();

            InputStream inStream = conn.getInputStream();
            return new String(readInputStream(inStream), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;
    }
}
