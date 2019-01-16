# 仅供公司项目使用，对外不提供服务。请勿下载

## FFUpdateSDK ![Download](https://api.bintray.com/packages/voisen/maven/ffupdate-release/images/download.svg)

android 自升级插件
### 联系作者 [voisein](mailto:voisen@icloud.com)

#### 功能简介:

1. 支持应用自升级
2. 支持Cordova项目自升级

### 使用方法

### 一、添加依赖

##### 1. 先在`project`的`build.gradle`中添加仓库地址(无法下载依赖时候添加)

````
	allprojects {
	    repositories {
	        google()
	        jcenter()
	        maven{
		        //需要添加的地址
	            url 'https://dl.bintray.com/voisen/maven/'
	        }
	    }
	}
````
*TIPS*: 配置多个Maven仓库

````
  		maven {
            url "https://maven.google.com"
        }
        
        maven {
            url "https://dl.bintray.com/voisen/maven"
        }
````

##### 2. 在`Modeule:app`的`build.gradle`中添加以下依赖:

````
//示例为1.0.2
	compile 'com.zhicheng:ffupdate-release:1.0.2'
````

### 二、项目代码引用

##### 1.创建自定义的Applicaction类，并根据需要加入以下代码(示例):
````
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //应用自升级配置
        FFUpdate.shareUpdate().registerAppKey("你的appkey",this);
        //检查应用更新
        FFUpdate.shareUpdate().checkUpdate();

        //cordova html应用资源包自升级配置
        CordovaResourceUpdate.shareUpdate().registerKey("你的appkey",this);
        //设置当前资源版本号,如果设置的当前版本号低于本地资源的版本号将不生效
        CordovaResourceUpdate.shareUpdate().setCurrentResourceVersion(12);
    }
}
````


##### 2. 当应用使用了Cordova的资源更新时，请在主控制器加上以下代码

````
  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化cordova的配置,在调用该代码的控制器必须继承`CordovaActivity`,请删除`loadUrl(launchUrl);`
        CordovaResourceUpdate.shareUpdate().initCordovaConfig(this);
    }
````

