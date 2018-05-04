package com.noriental.messageweb.message.vo;

import com.noriental.validate.bean.BaseRequest;

import java.util.List;
/**
 * Created by dongyl on 1/19/17.
 */
public class MessageRequest extends BaseRequest {

    //     curl -X POST $url -d “tag=UUU777asd9nnd&=content=xxx&tos=18611112222,18611112223"

    private String content;
    private List<String> mobiles;
    private String tag;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getMobiles() {
        return mobiles;
    }

    public void setMobiles(List<String> mobiles) {
        this.mobiles = mobiles;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
