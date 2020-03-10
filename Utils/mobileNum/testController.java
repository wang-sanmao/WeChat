package com.example.demo;

import com.alibaba.fastjson.JSON;
import org.springframework.util.ObjectUtils;

import java.util.Map;

public class testController {
    public static void main(String[] args) {
    }

    public String getMobileNumber() {
        String decryptS5 = "";
        try {
            /**
             *    data  =   userRequest.getResponse()  :报文
             *    sessionKey = sessionKey
             *    userRequest.getSign() = iv : 签名
             */
            decryptS5 = WXMobileNumberUtil.decryptS5(
                    "",
                    "UTF-8",
                    "",
                    "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addUserMobileNumber(decryptS5);
    }

    private String addUserMobileNumber(String decryptS5) {
        String phoneNumber = "";
        Map<String, Object> map = (Map) JSON.parse(decryptS5);
        if (!ObjectUtils.isEmpty(map)) {
            if (!ObjectUtils.isEmpty(map.get("phoneNumber"))) {
                phoneNumber = (String) map.get("phoneNumber");
            }
        }
        return phoneNumber;
    }
}
