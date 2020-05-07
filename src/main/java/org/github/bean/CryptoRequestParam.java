/**
 * Copyright DingXuan. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.github.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author zhangmingyang
 * @Date: 2020/5/6
 * @company Dingxuan
 */
public class CryptoRequestParam {
    /**
     * 请求ID,唯一标识
     */
    @JSONField(name = "request_id")
    private int requestId;
    /**
     * 请求类型
     */
    @JSONField(name = "request_type")
    private int requestType;
    /**
     * 请求数据
     */
    @JSONField(name = "request_data")
    private String requsetData;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public String getRequsetData() {
        return requsetData;
    }

    public void setRequsetData(String requsetData) {
        this.requsetData = requsetData;
    }



    public static void main(String[] args) {
        CryptoRequestParam cryptoRequestParam = new CryptoRequestParam();
        cryptoRequestParam.setRequestId(001);
        cryptoRequestParam.setRequestType(100);
        cryptoRequestParam.setRequsetData("123");
        System.out.println(JSON.toJSONString(cryptoRequestParam));
    }
}
