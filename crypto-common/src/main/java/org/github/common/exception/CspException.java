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
package org.github.common.exception;

/**
 * @author zhangmingyang
 * @Date: 2020/6/8
 * @company Dingxuan
 */
public class CspException extends CryptoException{
    private static final String MODULE_NAME="[Csp]";

    public CspException() {
    }

    public CspException(String message) {
        super(MODULE_NAME+message);
    }

    public CspException(String message, Throwable cause) {
        super(MODULE_NAME+message, cause);
    }

    public CspException(Throwable cause) {
        super(cause);
    }

    public CspException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(MODULE_NAME+message, cause, enableSuppression, writableStackTrace);
    }
}