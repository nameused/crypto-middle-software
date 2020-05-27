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
package org.github.common.utils;

import com.alibaba.fastjson.JSON;
import org.github.bean.AsymmetricKeyPair;

/**
 * @author zhangmingyang
 * @Date: 2020/5/26
 * @company Dingxuan
 */
public class MessageUtil {

    /**
     * 解析通用返回消息
     *
     * @param json
     * @return
     */
    public static String parseCommonResult(String json) {
        String data = JSON.parseObject(json).getString("data");
        String result = JSON.parseObject(data).getString("result");
        return result;
    }

    /**
     * 解析非对称密钥消息
     *
     * @param json
     * @return
     */
    public static AsymmetricKeyPair parseAsymmetricKeyPairResult(String json) {
        String data = JSON.parseObject(json).getString("data");
        String privateKey = JSON.parseObject(data).getString("private_key");
        String publicKey = JSON.parseObject(data).getString("public_key");
        AsymmetricKeyPair asymmetricKeyPair = new AsymmetricKeyPair(privateKey, publicKey);
        return asymmetricKeyPair;
    }
}
