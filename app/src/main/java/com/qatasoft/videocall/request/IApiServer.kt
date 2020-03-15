package com.qatasoft.videocall.request

import com.qatasoft.videocall.data.db.entities.Token
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IApiServer {

    //https://videocallkotlin.herokuapp.com/access_token?channel=hellobro&uid=1234

    @GET("access_token")
    fun getToken(@Query("channel")channel: String?,@Query("uid")uid:Int?): Call<Token>

}