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
 * appkey请求
 *
 * @author zhangmingyang
 * @Date: 2020/5/8
 * @company Dingxuan
 */
public class AppKeyRequest {
    @JSONField(name = "request_id")
    private int requestId;
    @JSONField(name = "app_name")
    private String appName;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public static void main(String[] args) {
        AppKeyRequest appRequestParam = new AppKeyRequest();
        appRequestParam.setRequestId(12334324);
        appRequestParam.setAppName("test1");
        System.out.println(JSON.toJSONString(appRequestParam));
    }
}
