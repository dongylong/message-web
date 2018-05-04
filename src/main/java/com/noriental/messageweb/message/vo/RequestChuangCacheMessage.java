package com.noriental.messageweb.message.vo;

import com.noriental.validate.bean.BaseRequest;

import javax.validation.constraints.NotNull;

public class RequestChuangCacheMessage extends BaseRequest {
    @NotNull
    private String mobile;
    @NotNull
    private String content;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
