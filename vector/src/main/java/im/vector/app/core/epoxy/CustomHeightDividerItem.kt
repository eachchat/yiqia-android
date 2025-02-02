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
package im.vector.app.core.epoxy

import android.view.View
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.app.R
import im.vector.app.core.utils.DimensionConverter
import im.vector.app.eachchat.base.BaseModule

@EpoxyModelClass(layout = R.layout.item_custom_height_divider)
abstract class CustomHeightDividerItem : VectorEpoxyModel<CustomHeightDividerItem.Holder>() {
    @EpoxyAttribute
    var customHeight: Int = 1

    @EpoxyAttribute
    var colorResource: Int = R.color.element_system_light

    override fun bind(holder: Holder) {
        super.bind(holder)

        val lp = holder.divider.layoutParams
        lp.height = DimensionConverter(holder.divider.resources).dpToPx(customHeight)
        holder.divider.layoutParams = lp
        holder.divider.setBackgroundColor(ContextCompat.getColor(BaseModule.getContext(), colorResource))
    }

    class Holder : VectorEpoxyHolder() {
        val divider by bind<View>(R.id.view_divider)
    }
}
