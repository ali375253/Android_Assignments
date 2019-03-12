package com.promactinfo.aliasagar.chatapplication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceBuilder {
    public static ChatApplicationService CAS;
    public static final String URL="https://chat.promactinfo.com/api/";
    //static GsonBuilder gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
    public static Retrofit.Builder builder=new Retrofit.Builder().baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create());
    public static Retrofit retrofit=builder.build();
    public static <S> S buildService(Class<S> serviceType){
        return retrofit.create(serviceType);
    }
}
