package com.example.jwtlogin.data.service

import com.example.jwtlogin.data.model.AuthenticationData
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthenticationService {

    @FormUrlEncoded
    @POST("/idp/api/v1/token")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password",
        @Field("client_id") clientId: String = "69bfdce9-2c9f-4a12-aa7b-4fe15e1228dc"
    ): Single<AuthenticationData>

    @FormUrlEncoded
    @POST("/idp/api/v1/token")
    fun refreshToken(
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("client_id") clientId: String = "69bfdce9-2c9f-4a12-aa7b-4fe15e1228dc"
    ): Single<AuthenticationData>
}