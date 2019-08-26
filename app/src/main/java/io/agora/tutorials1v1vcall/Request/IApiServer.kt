package io.agora.tutorials1v1vcall.Request

import io.agora.tutorials1v1vcall.models.Token
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.nio.channels.spi.AbstractInterruptibleChannel
import java.util.*

interface IApiServer {

    //https://videocallkotlin.herokuapp.com/access_token?channel=hellobro&uid=1234

    @GET("access_token")
    fun getToken(@Query("channel")channel: String?,@Query("uid")uid:Int?): Call<Token>

}