/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.home

import androidx.annotation.StringRes
import im.vector.app.R

enum class RoomListDisplayMode(@StringRes val titleRes: Int) {
    NOTIFICATIONS(R.string.bottom_action_notification),
    PEOPLE(R.string.title_action_people_x),
    ROOMS(R.string.contacts_book),
    INVITE(R.string.invite),
    FILTERED(/* Not used */ 0)
}
