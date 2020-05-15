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
package org.github.config;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 配置文件工厂
 *
 * @author zhangmingyang
 * @Date: 2020/5/14
 * @company Dingxuan
 */
public class CryptoConfigFactory {
    private static final Logger log = Logger.getLogger(CryptoConfigFactory.class);
    private static CryptoConfig cryptoConfig;

    public static CryptoConfig getCryptoConfig() {
        if (cryptoConfig == null) {
            synchronized (CryptoConfig.class) {
                if (cryptoConfig == null) {
                    cryptoConfig = loadCryptoConfig();
                }
            }
        }
        return cryptoConfig;
    }


    public static CryptoConfig loadCryptoConfig() {
        Yaml yaml = new Yaml();
        InputStream is = null;
        CryptoConfig cryptoConfig = null;
        try {
            String name = CryptoConfigFactory.class.getClassLoader().getResource(CryptoConfig.CRYPTO_CONFIG_PATH).getFile();
            is = new FileInputStream(name);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }
        cryptoConfig = yaml.loadAs(is, CryptoConfig.class);
        return cryptoConfig;
    }

    public static void main(String[] args) {
        System.out.println(getCryptoConfig().getServer().get("privatekey"));
        System.out.println(getCryptoConfig().getServer().get("privatekey"));
    }

}
