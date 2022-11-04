package com.dowell.dowellmap.activity

import com.dowell.dowellmap.data.model.Login
import com.dowell.dowellmap.data.network.ApiService
import com.dowell.dowellmap.data.network.LoginServiceBuilder
import retrofit2.Call
import retrofit2.Response

class LoginViewModel{
    fun loginUser(userData: Login, onResult: (Login?) -> Unit){
        val retrofit = LoginServiceBuilder.buildService(ApiService::class.java)
        retrofit.loginUser(userData).enqueue(
            object: retrofit2.Callback<Login> {
                override fun onFailure(call: Call<Login>, t: Throwable) {
                    onResult(null)
                }
                override fun onResponse(call: Call<Login>, response: Response<Login>) {
                    val addedUser = response.body()
                    onResult(addedUser)
                }
            }
        )
    }
}