### productFlavors配置

起因是项目有个多环境切换的需求，多个baseUrl的环境，测试确实不方便，需要来回卸载安装。开发也要改环境运行。所以才有了以下配置。

#### 配置app下build.gradle

在`defaultConfig`节点下新增一个`flavorDimensions`，增加一个构建的维度，值为：`channel`。后面按照配置增加`productFlavors`节点，下面就可以配置不同的构建信息了，下面我加入了三个指定的构建：beta、product、uat。因为目的是一次打包生成三个apk，可同时安装在手机上，方便测试。

> productFlavors构建必须指定一个dimension维度，值必须是之前定义在defaultConfig下flavorDimensions 的。

```groovy
android {
  ....
    defaultConfig {
      ....
        flavorDimensions "channel"
    }
    productFlavors {
        beta {//测试环境
            dimension "channel"
            buildConfigField "int", "ENV_TYPE", "1"
            buildConfigField "String", "BASE_URL", "\"http://api.zhouhaoh.com/test/\""
            applicationIdSuffix ".beta"
            manifestPlaceholders = [
                    app_name   : "@string/app_name_beta",
                    app_icon   : "@mipmap/ic_launcher",
                    pgyer_appid: "pgyer_appid_01"
            ]
            resValue("string", "envTag", "beta环境")
            resValue("drawable", "logo", "@drawable/ic_launcher_foreground_beta")
        }
        product {//生产环境
            dimension "channel"
            buildConfigField "int", "ENV_TYPE", "2"
            buildConfigField "String", "BASE_URL", "\"http://api.zhouhaoh.com/product/\""
            manifestPlaceholders = [
                    app_name   : "@string/app_name",
                    app_icon   : "@drawable/ic_launcher",
                    pgyer_appid: "pgyer_appid_02"
            ]
            resValue("string", "envTag", "生产环境")
            resValue("drawable", "logo", "@drawable/ic_launcher_foreground_pro")
        }
        uat {//uat环境
            dimension "channel"
            buildConfigField "int", "ENV_TYPE", "3"
            buildConfigField "String", "BASE_URL", "\"http://api.zhouhaoh.com/uat/\""
            applicationIdSuffix ".uat"
            manifestPlaceholders = [
                    app_name   : "@string/app_name_uat",
                    app_icon   : "@mipmap/ic_launcher",
                    pgyer_appid: "pgyer_appid_03"
            ]
            resValue("string", "envTag", "uat环境")
            resValue("drawable", "logo", "@drawable/ic_launcher_foreground_uat")
        }
    }

}
```

上述gradle配置中，各个节点含义：

1. `buildConfigField` 会生成在`BuildConfig.java`文件中，可以直接使用。
2. `applicationIdSuffix` 包名后缀，在构建时候，会动态替换。不配置就使用`defaultConfig`中的包名。
3. `manifestPlaceholders` 可以在清单文件中 使用 `android:label="${app_name}"` $引用此配置。详见`AndroidManifest.xml` （配置如下）
4. `resValue` 可以配置构建版本不同的资源值，`string` ，`drawable`等。



#### AndroidManifest配置

通过`${..}`的方式，引用`manifestPlaceholders`中定义的变量。

```xml
 <application
            android:allowBackup="true"
            android:icon="@mipmap/ic_launcher"
            android:label="${app_name}"
            android:roundIcon="@mipmap/ic_launcher_round"
            android:supportsRtl="true"
            android:theme="@style/AppTheme"
            tools:ignore="GoogleAppIndexingWarning">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>

                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
        <meta-data
                android:name="PGYER_APPID"
                android:value="${pgyer_appid}"/>
    </application>
```



#### 指定运行类型

在Build Varants中选择要运行的构建变体的类型（如下图），点击run就可以啦。

![](https://raw.githubusercontent.com/zhouhaoo/flavors/master/screenshot/20190128162617.png)

运行后：

![](https://raw.githubusercontent.com/zhouhaoo/flavors/master/screenshot/device-2019-01-28-161918.png)


#### 实际效果

根据不同的配置 三个apk可以共存。因为包名已经不同了。

|                   生产环境                   | uat环境 | 测试环境 |
| :------------------------------------------: | :-----: | :------: |
| ![](https://raw.githubusercontent.com/zhouhaoo/flavors/master/screenshot/device-2019-01-28-161946.png)|![](https://raw.githubusercontent.com/zhouhaoo/flavors/master/screenshot/device-2019-01-28-162015.png)|![](https://raw.githubusercontent.com/zhouhaoo/flavors/master/screenshot/device-2019-01-28-161959.png)|

### 蒲公英上传

自动配置好之后，方便代码更新到测试，我集成了蒲公英的sdk到app上，通过不同的`AndroidManifest`配置`meta-data` 并编写shell脚本 自动上传。

#### gradle打包设置

在android节点下，变更打包生成包的位置。

```groovy
android {
  ....
    applicationVariants.all { variant ->
        if (variant.buildType.name != "debug") {
            variant.getPackageApplication().outputDirectory = new File(project.rootDir.absolutePath + "/apk")
        }
        variant.getPackageApplication().outputScope.apkDatas.forEach { apkData ->
            apkData.outputFileName = "${variant.productFlavors[0].name}-v${variant.versionName}.apk"
        }
    }
}
```

#### shell脚本

各个版本打包完成后，可以直接执行工程下shell脚本`uplaod.sh`，命令:`sh upload.sh` ,参数`_api_key`请进入自己账户查看替换。如果结合`jenkins`等自动构建工具 效果更佳。实际开发中只需关注代码即可。

提交代码 -> jenkins监控代码更新 -> 构建不同版本apk -> 构建完成执行脚本上传蒲公英平台



```shell
# upload.sh by zhouhaoh
function upload()
{
curl -F "file=@$1" -F '_api_key=db181f421441121223126a11c7f5bf68' -F "buildUpdateDescription=本次更新信息" https://www.pgyer.com/apiv2/app/upload
}
cd apk/
for file in `ls`
do
 if [[ $file =~ \.apk$ ]] ;then
    upload $file
 fi
done
```

### 总结

通过配置不同的构建类型，可以使不同的apk共存在手机上，例如base_url等，一次打包，上传测试平台。后续只要关注代码编写即可。