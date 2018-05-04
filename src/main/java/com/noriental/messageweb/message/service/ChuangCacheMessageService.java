package com.noriental.messageweb.message.service;

import com.noriental.messageweb.message.vo.RequestChuangCacheMessage;
import com.noriental.validate.bean.CommonDes;
import com.noriental.validate.exception.BizLayerException;

/**
 * @author dongyl
 * @date 2017/9/6
 */
public interface ChuangCacheMessageService {

    /**
     * http://wiki.okjiaoyu.cn/pages/viewpage.action?pageId=3735961#message-svr接口定义-3.1发送短信
     * @param request
     * @return
     * @throws BizLayerException
     */
    CommonDes sendChuangCacheMessage(RequestChuangCacheMessage request) throws BizLayerException;
}
