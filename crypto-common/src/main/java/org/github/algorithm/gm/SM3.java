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
package org.github.algorithm.gm;

import org.github.common.exception.HashException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zhangmingyang
 * @Date: 2020/4/27
 * @company Dingxuan
 */
public class SM3 extends GmBase {
    private static final Logger log = Logger.getLogger(SM3.class);
    public byte[] hash(byte[] data) throws HashException {
        if (ArrayUtils.isEmpty(data)) {
            throw new HashException("Some input is empty");
        }
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("sm3");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            throw new HashException(e);
        }
        messageDigest.update(data);
        return messageDigest.digest();
    }
}
