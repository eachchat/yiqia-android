/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.yiqia.complain.api

import im.vector.app.yiqia.complain.data.ComplainInput
import im.vector.app.yiqia.net.data.Response
import im.vector.app.yiqia.net.retrofit.RetrofitManager
import org.matrix.android.sdk.api.session.Session
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Created by zhouguanjie on 2021/4/15.
 */
interface ComplainService {

    @GET("/api/services/global/v1/constant/{constantCode}")
    suspend fun getComplains(@Path("constantCode") constantCode: String): Response<Any?, ArrayList<String>?>

    @POST("/api/services/global/v1/complain")
    suspend fun complain(@Body input: ComplainInput): Response<Any?, Any?>

    companion object {
        fun getInstance(homeServerUrl: String, session: Session): ComplainService? =
                RetrofitManager.instance.getMatrixRetrofit(homeServerUrl, session).create(ComplainService::class.java)
    }

}
