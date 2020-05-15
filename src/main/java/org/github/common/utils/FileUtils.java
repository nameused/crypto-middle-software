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


import org.apache.log4j.Logger;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.github.common.exception.SignException;
import org.github.config.CryptoConfigFactory;
import java.io.*;


/**
 * 文件辅助类
 *
 * @author zhangmingyang
 * @Date: 2020/5/14
 * @company Dingxuan
 */
public class FileUtils {
    private static final Logger log = Logger.getLogger(FileUtils.class);
    private static final String SUFFIX = ".pem";

    /**
     * 在特定目录中根据appCode
     * 查找文件夹中的文件
     *
     * @param appCode
     * @return
     */
    public static boolean findFile(String appCode) {
        String appKeyPath = CryptoConfigFactory.getCryptoConfig().getClient().get("appKeyPath");
        if (new File(appKeyPath + appCode + SUFFIX).exists()) {
            return true;
        }
        return false;
    }


    /**
     * 根据appCode生成文件名称
     * fileContent为文件内容
     *
     * @param appCode
     * @param fileContent
     */
    public static void genAppKeyFile(String path, String appCode, byte[] fileContent) throws IOException {
        if (new File(path + appCode).exists()) {
            log.error("该文件已存在!");
            throw new IOException("该文件已存在!");
        }
        PemObject pemObject = new PemObject("PRIVATE KEY", fileContent);
        StringWriter str = new StringWriter();
        PemWriter pemWriter = new PemWriter(str);
        try {
            pemWriter.writeObject(pemObject);
            pemWriter.close();
            str.close();
            PrintWriter pw = new PrintWriter(new FileOutputStream(path + appCode + SUFFIX));
            String privateKey = new String(str.toString());
            pw.print(privateKey);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据文件名称获取appKey密钥
     *
     * @param appCode
     */
    public static String getAppKey(String appCode) {
        String appKeyPath = CryptoConfigFactory.getCryptoConfig().getClient().get("appKeyPath");
        File inFile = new File(appKeyPath + appCode + SUFFIX);
        long fileLen = inFile.length();
        Reader reader = null;
        PemObject pemObject = null;
        char[] content = null;
        try {
            reader = new FileReader(inFile);
            content = new char[(int) fileLen];
            reader.read(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = new String(content);
        String privateKeyPEM = str.replace("-----BEGIN PRIVATE KEY-----\r", "")
                .replace("-----END PRIVATE KEY-----\r", "").replace("\n","").replace("\r","");
        return privateKeyPEM;
    }

    public static void main(String[] args) throws SignException, IOException {
//        SM2 sm2 = new SM2();
//        KeyPair keyPair = sm2.genKeyPair();
//        System.out.println(Base64.toBase64String(keyPair.getPrivate().getEncoded()));
//        genAppKeyFile("D:/crypto/appkey/", "app1", keyPair.getPrivate().getEncoded());
        String a = getAppKey("office2");
        System.out.println(a);

    }
}
