package com.peihou.warmer.http;


import com.google.gson.Gson;
import com.peihou.warmer.base.MyApplication;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by whd on 2017/12/23.
 */

public class HttpUtils {

    public static String ipAddress = "http://47.110.132.149:8098/app/";


    static Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {

            Request request = chain.request();
            //网上很多示例代码都对在request请求前对其进行无网的判断，其实无需判断，无网自动访问缓存
//            if(!NetworkUtil.getInstance().isConnected()){
//                request = request.newBuilder()
//                        .cacheControl(CacheControl.FORCE_CACHE)//只访问缓存
//                        .build();
//            }
            Response response = chain.proceed(request);

            if (NetWorkUtil.isConn(MyApplication.getContext())) {
                int maxAge = 0;//缓存失效时间，单位为秒
                return response.newBuilder()
                        .removeHeader("Pragma")//清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                        .header("Cache-Control", "public ,max-age=" + maxAge)
                        .build();
            } else {
//                NetWorkUtil.showNoNetWorkDlg(MyApplication.getContext());
//                这段代码设置无效
//                int maxStale = 60 * 60 * 24 * 28; // 无网络时，设置超时为4周
//                return response.newBuilder()
//                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
//                        .removeHeader("Pragma")
//                        .build();
            }
            return response;
        }
    };

    public static String baseUrl = "http://47.110.132.149:8098/app/";
    private static String weatherUrl="http://apicloud.mob.com/v1/weather/";

    /**
     * POST请求
     * @param url
     * @param params
     * @return
     */
    public static String requestPost(String url, Map<String, Object> params) {
        String result = null;
        try {
//            File httpCacheDirectory = new File(MyApplication.getContext().getCacheDir(), "HttpCache");//这里为了方便直接把文件放在了SD卡根目录的HttpCache中，一般放在context.getCacheDir()中
//            int cacheSize = 10 * 1024 * 1024;//设置缓存文件大小为10M
//            Cache cache = new Cache(httpCacheDirectory, cacheSize);
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(baseUrl)
                    .client(new OkHttpClient.Builder()
                            .connectTimeout(3, TimeUnit.SECONDS)//设置连接超时
                            .readTimeout(5, TimeUnit.SECONDS)//读取超时
                            .writeTimeout(5, TimeUnit.SECONDS)//写入超时
                            .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)//添加自定义缓存拦截器（后面讲解），注意这里需要使用.addNetworkInterceptor
//                            .cache(cache)//把缓存添加进来
                            .build())
                    .build();
            HttpService httpService = retrofit.create(HttpService.class);
            String CONTENT_TYPE = "application/json";
            Gson gson = new Gson();
            String content = gson.toJson(params);
            RequestBody body = RequestBody.create(MediaType.parse(CONTENT_TYPE), content);
            Call<ResponseBody> call=httpService.postRequest(url,body);
            retrofit2.Response<ResponseBody> response = call.execute();
            result = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * get请求
     * @param url
     * @param code
     * @return
     */
    public static String requestGet(String url,int code) {
        String result = null;
        try {
            String baseUrl2="";
            if (code==1){
                baseUrl2=weatherUrl;
            }else if (code==2){
                baseUrl2=baseUrl;
            }
//            File httpCacheDirectory = new File(MyApplication.getContext().getCacheDir(), "HttpCache");//这里为了方便直接把文件放在了SD卡根目录的HttpCache中，一般放在context.getCacheDir()中
//            int cacheSize = 10 * 1024 * 1024;//设置缓存文件大小为10M
//            Cache cache = new Cache(httpCacheDirectory, cacheSize);
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(baseUrl2)
                    .client(new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.SECONDS)//设置连接超时
                    .readTimeout(5, TimeUnit.SECONDS)//读取超时
                    .writeTimeout(5, TimeUnit.SECONDS)//写入超时
                    .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)//添加自定义缓存拦截器（后面讲解），注意这里需要使用.addNetworkInterceptor
                    .build())
                    .build();
            HttpService httpService = retrofit.create(HttpService.class);
            Call<ResponseBody> call=httpService.getRequest(url);
            retrofit2.Response<ResponseBody> response = call.execute();
            result = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     *
     * 上传个人信息和头像文件
     * @param paramsMap
     * @param file
     * @return
     */
    public static String uploadFile(Map<String,Object> paramsMap, File file){
        String result=null;
        try {
            Retrofit retrofit=new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(baseUrl)
                    .build();
            HttpService userService=retrofit.create(HttpService.class);
            // 创建 RequestBody，用于封装构建RequestBody

            // MultipartBody.Part  和后端约定好Key，这里的partName是用image

            // 执行请求

//            Map<String, RequestBody> requestBodyMap = new HashMap<>();
//            for (Map.Entry<String, Object> entry:paramsMap.entrySet()){
//                String key=entry.getKey();
//                String value=entry.getValue()+"";
//                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), value);
//                requestBodyMap.put(key, requestBody);
//            }


            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("image/jpg"), file);
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            Call<ResponseBody> call = userService.uploadFile(paramsMap,body);
            retrofit2.Response<ResponseBody> response=call.execute();
            boolean success=response.isSuccessful();
            if (success){
                result=response.body().string();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * POST请求FORM表单
     * 上传多个文件和多个字段
     * @param url
     * @param paramsMap
     * @param filesMap
     * @return
     */
    public static String upLoadFileAndDesc(String url,Map<String,Object> paramsMap,Map<String,Object> filesMap){
        String result=null;
        try {
            Retrofit retrofit=new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(baseUrl)
                    .build();
            HttpService userService=retrofit.create(HttpService.class);

            MultipartBody.Builder requestBodyMap = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (Map.Entry<String,Object> entry:paramsMap.entrySet()){
                String key=entry.getKey();
                String value=entry.getValue()+"";
                requestBodyMap.addFormDataPart(key,value);
            }
            //入参-文件
            for (Map.Entry entry : filesMap.entrySet()) {
                File file = (File) entry.getValue();
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                String fileName = file.getName();
                requestBodyMap.addFormDataPart("files", fileName, requestFile);
            }
            retrofit2.Call<ResponseBody> call = userService.upLoadFileAndDesc(url,requestBodyMap.build());
            retrofit2.Response<ResponseBody> response=call.execute();
            boolean success=response.isSuccessful();
            if (success){
                result=response.body().string();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
