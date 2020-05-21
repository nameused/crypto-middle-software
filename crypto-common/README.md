### 1.密码服务中间件-公共组件介绍
1.包含工具类

2.国密算法软实现

3.密码请求与返回数据格式

4.异常处理

5.签名因子实现

### 2.密码服务中间件-公共组件调用方式
步骤：

1.首先将jar包导入到工程lib目录下

2.pom.xml文件中添加如下配置 (注:版本号根据实际版本号引入)

    <dependency>
        <groupId>com.github</groupId>
        <artifactId>crypto-common</artifactId>
        <version>0.1</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/lib/crypto-client-0.1.jar</systemPath>
    </dependency>

    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcprov-jdk15on</artifactId>
        <version>1.60</version>
    </dependency>
 3.安全摘要调用代码如下：
 
    SecurityDigest securityDigest = new SecurityDigest();
    //1.传入appid,生成appkey
    byte[] appkey=securityDigest.genAppkey("Test");
    
    //2.生成签名因子
    byte[] signFactor=securityDigest.genSignFactor();
    
    //3.根据签名因子与appkey生成keyS
    byte[] keyS=securityDigest.genKeyS(signFactor,appkey);
 
    //4.利用keyS和传入数据进行hmac计算
    byte[] hamc=securityDigest.hamc(keyS,"test".getBytes());
 
    //5.验证hmac
    securityDigest.verify(signFactor,appkey,"test".getBytes(),hmac);
   
   
   