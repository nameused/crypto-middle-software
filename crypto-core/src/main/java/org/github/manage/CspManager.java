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
package org.github.manage;

import org.apache.log4j.Logger;
import org.github.csp.ICsp;
import org.github.csp.gm.GmCspFactory;
import org.github.factory.ICspFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangmingyang
 * @Date: 2020/6/8
 * @company Dingxuan
 */
public class CspManager {
    private static final Logger log = Logger.getLogger(CspManager.class);
    private static CspManager instance;
    /**
     * 默认使用的Csp
     */
    private ICsp defaultCsp = null;
    /**
     * 全部可用的Csp集合
     */
    private Map<String, ICsp> cspMap = new HashMap<>();

    private static volatile boolean isInit;

    public static CspManager getInstance() {
        if (instance == null) {
            synchronized (CspManager.class) {
                if (instance == null) {
                    instance = new CspManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化csp工厂
     *
     * @param providerList
     * @param defaultProvider
     */
    public static void initCspFactories(List<String> providerList, String defaultProvider) {

        if (isInit) {
            log.warn("Can init only once");
            return;
        }
        isInit = true;
        CspManager cspManager = CspManager.getInstance();
        for (String s : providerList) {
            ICspFactory factory = null;
            if (s.equalsIgnoreCase("gm")) {
                factory = new GmCspFactory();
            } else if (s.equalsIgnoreCase("sdvs")) {
                factory = null;
            }
            cspManager.initCsp(factory, s, defaultProvider);
        }
    }

    /**
     * 初始化CSP
     *
     * @param factory
     * @param cspProvider
     * @param defaultProvider
     */
    private void initCsp(ICspFactory factory, String cspProvider, String defaultProvider) {
        ICsp csp = factory.getCsp();
        cspMap.put(cspProvider, csp);

        if (cspProvider.equalsIgnoreCase(defaultProvider)) {
            defaultCsp = csp;
        }
    }

    /**
     * 获取默认的CSP
     *
     * @return
     */
    public static ICsp getDefaultCsp() {
        if (!isInit) {
            log.warn("Before using CSP, please call initCspFactories(). Falling back to bootCsp.");
            initCspFactories();
        }
        return getInstance().defaultCsp;
    }


    public static void initCspFactories() {
        List<String> providerList=new ArrayList<>();
        providerList.set(0,"gm");
        providerList.set(1,"dsvs");
        initCspFactories(providerList,"gm");
    }


}
