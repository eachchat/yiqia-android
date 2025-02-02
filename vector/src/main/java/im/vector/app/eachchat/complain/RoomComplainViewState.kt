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

package im.vector.app.eachchat.complain

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import im.vector.app.core.platform.GenericIdArgs
import im.vector.app.features.roomprofile.RoomProfileArgs
import org.matrix.android.sdk.api.session.room.model.RoomSummary

data class RoomComplainViewState (
        val roomId: String,
        val matrixId: String? = null,
        val roomSummary: Async<RoomSummary> = Uninitialized,
        val complainList: ArrayList<String>? = null,
        val isLoading: Boolean = false,
        val canConfirm: Boolean = false
        ) : MavericksState {

    constructor(args: RoomProfileArgs) : this(roomId = args.roomId)

    constructor(args: GenericIdArgs) : this(roomId = args.id)
}
