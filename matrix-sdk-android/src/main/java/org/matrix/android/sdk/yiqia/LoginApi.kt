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

package org.matrix.android.sdk.yiqia

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface LoginApi {

    companion object {
        private const val GMS_URL = "https://gms.yiqia.com";

        fun getInstance(): LoginApi = RetrofitManager.instance.getMatrixRetrofit(GMS_URL).create(LoginApi::class.java)
    }

    @POST("/api/services/global/v1/configuration")
    suspend fun gms(@Body input: OrgSearchInput): Response<GMSResult?, Any?>

    @POST("/api/services/global/v1/tenant/names")
    suspend fun orgNames(@Body input: OrgSearchInput): Response<Any?, List<String>>

    @GET
    suspend fun authSettings(@Url url: String): Response<AuthSettingResult?, Any?>
}

data class OrgSearchInput(val tenantName: String)

data class GMSResult(
        val defaultIdentity: Identity,
        val entry: Entry,
        val mqtt: Entry,
        val matrix: Matrix?,
        val im: IMConfig?,
        val favorite: FavoriteConfig?,
        val book: BookConfig?,
)

data class Identity(val identityType: String?)

data class Matrix(val homeServer: String)

data class IMConfig(val videoSwitch: Boolean, val audioSwitch: Boolean)

data class FavoriteConfig(val favSwitch: Boolean)

data class BookConfig(val contactSwitch: Boolean, val groupSwitch: Boolean, val orgSwitch: Boolean)

data class Entry(val host: String, val port: String, val tls: Boolean) {
    val cooperationUrl: String
        get() = "${if (tls) "https" else "http"}://$host:$port"
//    val mqttUrl: String
//        get() = "tcp://$host:$port"
}

data class AuthSettingResult(
        val authType: String,
        val threeAuthType: String?,
        val userNamePlaceHolder: String?,
        val passwordPlaceHolder: String?,
        val matrixEmail: String?,
        val threeEmail: String?,
        val initPasswordRegex: String?,
        val passwordChangeInfo: String?
)
