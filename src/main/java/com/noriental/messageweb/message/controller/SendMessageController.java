package com.noriental.messageweb.message.controller;

import com.noriental.messageweb.message.service.ChuangCacheMessageService;
import com.noriental.messageweb.message.vo.RequestChuangCacheMessage;
import com.noriental.messageweb.message.vo.ResponseStatus;
import com.noriental.messageweb.message.vo.ResponseText;
import com.noriental.utils.json.JsonUtil;
import com.noriental.utils.text.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by dongyl on 1/19/17.
 */
@Controller
@RequestMapping("/message")
public class SendMessageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource
    private ChuangCacheMessageService chuangCacheMessageService;

//    private static final String COMPANY_TAG = "【OKAY教育】";
    @Resource
    private com.noriental.messageweb.message.vo.Config config; //引用统一的参数配置类

    /**
     * 目前我们内部没有统一的http发短信接口，申请开发一个HTTP的短信接口，需求如下：
     * 1、请求方式为：post
     * 2、支持短信分组识别（即tag，可以用12位以上的大小写和数字组成），各组使用需要单独开通，识别不通过即返回error
     * 3、短信内容、发送时间及来源(IP地址)、接收人、tag需要入库存档
     * 4、支持队列，防止短信接口拥塞，我们需要对短信接口添加邮件报警。
     * 5、发送成功返回success、发送失败返回fail。
     * <p>
     * 使用方式如下：
     * <p>
     * method: post
     * params:
     * - tag:使用标识
     * - content: 短信内容
     * - tos: 使用逗号分隔的多个手机号
     * 使用方法：url=http短信接口
     * curl -X POST $url -d “tag=UUU777asd9nnd&=content=xxx&tos=18611112222,18611112223"
     * curl -X POST $url -d “tag=uaksjdnkUd8823&=content=xxx&tos=18611112222,18611112223"
     *
     * @param request req
     * @return success/fail
     */
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    @ResponseBody
    public ResponseText sendMessage(HttpServletRequest request,
                                    @RequestParam(value = "tag", required = false) String tag,
                                    @RequestParam(value = "tos") String phones,
                                    @RequestParam(value = "content") String content) {
        RequestChuangCacheMessage req = new RequestChuangCacheMessage();

        Map<String, String> map = new HashMap<>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        String dateStr = format.format(d);

        ResponseText result = new ResponseText();

        Map<String, String[]> reqMap = request.getParameterMap();
        if (MapUtils.isEmpty(reqMap)
                || !reqMap.containsKey("tos")
                || !reqMap.containsKey("content")) {
            LOGGER.error("入参为空");
            result.setMessage(ResponseStatus.FAILED.getStatus());
            return result;
        }

        String tos = phones.replace(" ", "").replace("，", ",");

        if (org.apache.commons.lang3.StringUtils.isBlank(tos)
                || !StringUtils.hasText(tos)) {
            LOGGER.error("入参Tos为空");
            result.setMessage(ResponseStatus.FAILED.getStatus());
            return result;
        }
        String[] mobileStrings = tos.split(",");
        List<String> availableMobiles = new ArrayList<>();
        List<String> mobiles = Arrays.asList(mobileStrings);
        if (CollectionUtils.isEmpty(mobiles)) {
            LOGGER.error("入参mobiles为空");
            result.setMessage(ResponseStatus.FAILED.getStatus());
            return result;
        } else {

            if (org.apache.commons.lang3.StringUtils.isBlank(content)
                    || !StringUtils.hasText(content)
                    || content.length() > 350) {
                LOGGER.info("content参数值不合法");
                result.setMessage(ResponseStatus.FAILED.getStatus());
                return result;
            }
            List<String> unAvailableMobiles = new ArrayList<>();
            for (String mobile : mobiles) {
                if (org.apache.commons.lang3.StringUtils.isBlank(mobile)
                        || (!StringUtils.hasText(mobile))
                        || (mobile.getBytes().length != 11)
                        ) {
                    unAvailableMobiles.add(mobile);
                } else {
                    availableMobiles.add(mobile);
                }
            }

            if (CollectionUtils.isNotEmpty(unAvailableMobiles)) {
                LOGGER.warn("unAvailableMobiles:{}", JsonUtil.obj2Json(unAvailableMobiles));
            }
            if (CollectionUtils.isEmpty(availableMobiles)) {
                LOGGER.error("没有符合条件的手机号码");
                result.setMessage(ResponseStatus.FAILED.getStatus());
                return result;
            }
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        ip = ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
        String existsIPs = config.server_exists_ip;
        if (org.apache.commons.lang3.StringUtils.isBlank(existsIPs)
                || !StringUtils.hasText(existsIPs)) {
            LOGGER.error("入参existsIPs为空");
            result.setMessage(ResponseStatus.FAILED.getStatus());
            return result;
        }
        String[] ips = existsIPs.split(",");
        List<String> ipList = Arrays.asList(ips);
        if (!ipList.contains(ip)) {
            String existsTags = config.server_exists_tag;
            if (org.apache.commons.lang3.StringUtils.isBlank(existsTags)
                    || !StringUtils.hasText(existsTags)) {
                LOGGER.error("入参existsTags为空");
                result.setMessage(ResponseStatus.FAILED.getStatus());
                return result;
            }
            String[] tags = existsTags.split(",");
            List<String> tagList = Arrays.asList(tags);
            if (MapUtils.isEmpty(reqMap)
                || !reqMap.containsKey("tag")) {
                LOGGER.error("入参为空");
                result.setMessage(ResponseStatus.FAILED.getStatus());
                return result;
            }

            if (org.apache.commons.lang3.StringUtils.isBlank(tag)
                    || !StringUtils.hasText(tag)) {
                LOGGER.error("入参tag为空");
                result.setMessage(ResponseStatus.FAILED.getStatus());
                return result;
            }
            if (CollectionUtils.isEmpty(tagList)) {
                LOGGER.error("tag config");
                result.setMessage(ResponseStatus.FAILED.getStatus());
                return result;
            }
            if (!tagList.contains(tag)) {
                LOGGER.error("tag not exists");
                result.setMessage(ResponseStatus.FAILED.getStatus());
                return result;
            }
            LOGGER.info("tag:{}", tag);
        }
        LOGGER.info("IP:{},mobiles:{},availableMobiles:{},content:{},dateStr:{}",
                ip, JsonUtil.obj2Json(mobiles), JsonUtil.obj2Json(availableMobiles), content, dateStr);
        req.setContent(content);
        req.setMobile(tos);
        try {
            chuangCacheMessageService.sendChuangCacheMessage(req);
        } catch (Exception e) {
            LOGGER.error("sendMessage 出错:", e);
            result.setMessage(ResponseStatus.FAILED.getStatus());
            return result;
        }
        result.setMessage(ResponseStatus.SUCCESS.getStatus());
        return result;
    }
}
