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

package im.vector.app.features.home.room.list

import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.amulyakhare.textdrawable.TextDrawable
import dagger.Lazy
import im.vector.app.ActiveSessionDataSource
import im.vector.app.EmojiCompatWrapper
import im.vector.app.EmojiSpanify
import im.vector.app.R
import im.vector.app.core.epoxy.ClickListener
import im.vector.app.core.epoxy.VectorEpoxyHolder
import im.vector.app.core.epoxy.VectorEpoxyModel
import im.vector.app.core.epoxy.onClick
import im.vector.app.core.extensions.setTextOrHide
import im.vector.app.core.resources.ColorProvider
import im.vector.app.core.resources.StringProvider
import im.vector.app.core.ui.views.PresenceStateImageView
import im.vector.app.core.ui.views.ShieldImageView
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.features.displayname.getBestName
import im.vector.app.features.displayname.getBestNameEachChat
import im.vector.app.features.home.AvatarRenderer
import im.vector.app.features.home.room.detail.timeline.format.DisplayableEventFormatter
import im.vector.app.features.home.room.detail.timeline.format.NoticeEventFormatter
import im.vector.app.features.home.room.detail.timeline.format.RoomHistoryVisibilityFormatter
import im.vector.app.features.html.EventHtmlRenderer
import im.vector.app.features.html.MatrixHtmlPluginConfigure
import im.vector.app.features.roomprofile.permissions.RoleFormatter
import im.vector.app.features.settings.VectorPreferences
import im.vector.app.features.themes.ThemeUtils
import im.vector.lib.core.utils.epoxy.charsequence.EpoxyCharSequence
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.gujun.android.span.span
import org.commonmark.node.Document
import org.matrix.android.sdk.api.crypto.RoomEncryptionTrustLevel
import org.matrix.android.sdk.api.session.events.model.EventType
import org.matrix.android.sdk.api.session.events.model.toModel
import org.matrix.android.sdk.api.session.presence.model.UserPresence
import org.matrix.android.sdk.api.session.room.model.message.MessageAudioContent
import org.matrix.android.sdk.api.session.room.model.message.MessagePollContent
import org.matrix.android.sdk.api.session.room.model.message.MessageTextContent
import org.matrix.android.sdk.api.session.room.model.message.MessageType
import org.matrix.android.sdk.api.session.room.model.relation.ReactionContent
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.room.timeline.getLastMessageContent
import org.matrix.android.sdk.api.session.room.timeline.getTextDisplayableContent
import org.matrix.android.sdk.api.util.MatrixItem
import javax.inject.Inject

@EpoxyModelClass(layout = R.layout.item_room)
abstract class RoomSummaryItem : VectorEpoxyModel<RoomSummaryItem.Holder>() {

    @EpoxyAttribute lateinit var typingMessage: String
    @EpoxyAttribute lateinit var avatarRenderer: AvatarRenderer
    @EpoxyAttribute lateinit var matrixItem: MatrixItem

    @EpoxyAttribute lateinit var lastFormattedEvent: EpoxyCharSequence
    @EpoxyAttribute lateinit var lastEventTime: String
    @EpoxyAttribute var encryptionTrustLevel: RoomEncryptionTrustLevel? = null
    @EpoxyAttribute var userPresence: UserPresence? = null
    @EpoxyAttribute var showPresence: Boolean = false
    @EpoxyAttribute var izPublic: Boolean = false
    @EpoxyAttribute var unreadNotificationCount: Int = 0
    @EpoxyAttribute var hasUnreadMessage: Boolean = false
    @EpoxyAttribute var hasDraft: Boolean = false
    @EpoxyAttribute var showHighlighted: Boolean = false
    @EpoxyAttribute var hasFailedSending: Boolean = false
    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash) var itemLongClickListener: View.OnLongClickListener? = null
    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash) var itemClickListener: ClickListener? = null
    @EpoxyAttribute var showSelected: Boolean = false
    @EpoxyAttribute open var isDm: Boolean = false
    @EpoxyAttribute var otherMemberIds: List<String>? = null
    @EpoxyAttribute var timelineEvent: TimelineEvent? = null

    private val stringProvider: StringProvider = StringProvider(BaseModule.getContext().resources)
    private val colorProvider: ColorProvider = ColorProvider(BaseModule.getContext())
    private val emojiSpanify: EmojiSpanify = EmojiCompatWrapper(BaseModule.getContext())
    private val noticeEventFormatter: NoticeEventFormatter = NoticeEventFormatter(
            ActiveSessionDataSource(),
            RoomHistoryVisibilityFormatter(stringProvider),
            RoleFormatter(stringProvider),
            VectorPreferences(BaseModule.getContext()),
            stringProvider
    )
    private val htmlRenderer: EventHtmlRenderer = EventHtmlRenderer(
            MatrixHtmlPluginConfigure(colorProvider, BaseModule.getContext().resources),
            BaseModule.getContext(),
            VectorPreferences(BaseModule.getContext())
    )

    @OptIn(DelicateCoroutinesApi::class)
    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.rootView.onClick(itemClickListener)
        holder.rootView.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            itemLongClickListener?.onLongClick(it) ?: false
        }
        holder.titleView.text = matrixItem.getBestName()
        if (isDm && otherMemberIds?.isNotEmpty() == true) {
            GlobalScope.launch(Dispatchers.IO)
            {
                otherMemberIds!![0].getBestNameEachChat (matrixItem.getBestName()) {
                    GlobalScope.launch(Dispatchers.Main) {
                        holder.titleView.text = it
                    }
                }
            }
        } else {
            holder.titleView.text = matrixItem.getBestName()
        }
        holder.lastEventTimeView.text = lastEventTime
        GlobalScope.launch(Dispatchers.IO) {
            var lastFormattedEventCharSequence = timelineEvent?.let { format(it, isDm, isDm.not()) }
            GlobalScope.launch(Dispatchers.Main) {
                holder.lastEventView.text = lastFormattedEventCharSequence
            }
        }
        // holder.lastEventView.text = lastFormattedEvent.charSequence
        holder.unreadCounterBadgeView.render(UnreadCounterBadgeView.State(unreadNotificationCount, showHighlighted))
        holder.unreadIndentIndicator.isVisible = hasUnreadMessage
        holder.draftView.isVisible = hasDraft
        avatarRenderer.render(matrixItem, holder.avatarImageView)
        holder.roomAvatarDecorationImageView.render(encryptionTrustLevel)
        holder.roomAvatarPublicDecorationImageView.isVisible = izPublic
        holder.roomAvatarFailSendingImageView.isVisible = hasFailedSending
        renderSelection(holder, showSelected)
        holder.typingView.setTextOrHide(typingMessage)
        holder.lastEventView.isInvisible = holder.typingView.isVisible
        holder.roomAvatarPresenceImageView.render(showPresence, userPresence)
    }

    override fun unbind(holder: Holder) {
        holder.rootView.setOnClickListener(null)
        holder.rootView.setOnLongClickListener(null)
        avatarRenderer.clear(holder.avatarImageView)
        super.unbind(holder)
    }

    private fun renderSelection(holder: Holder, isSelected: Boolean) {
        if (isSelected) {
            holder.avatarCheckedImageView.visibility = View.VISIBLE
            val backgroundColor = ThemeUtils.getColor(holder.view.context, R.attr.colorPrimary)
            val backgroundDrawable = TextDrawable.builder().buildRound("", backgroundColor)
            holder.avatarImageView.setImageDrawable(backgroundDrawable)
        } else {
            holder.avatarCheckedImageView.visibility = View.GONE
            avatarRenderer.render(matrixItem, holder.avatarImageView)
        }
    }

    class Holder : VectorEpoxyHolder() {
        val titleView by bind<TextView>(R.id.roomNameView)
        val unreadCounterBadgeView by bind<UnreadCounterBadgeView>(R.id.roomUnreadCounterBadgeView)
        val unreadIndentIndicator by bind<View>(R.id.roomUnreadIndicator)
        val lastEventView by bind<TextView>(R.id.roomLastEventView)
        val typingView by bind<TextView>(R.id.roomTypingView)
        val draftView by bind<ImageView>(R.id.roomDraftBadge)
        val lastEventTimeView by bind<TextView>(R.id.roomLastEventTimeView)
        val avatarCheckedImageView by bind<ImageView>(R.id.roomAvatarCheckedImageView)
        val avatarImageView by bind<ImageView>(R.id.roomAvatarImageView)
        val roomAvatarDecorationImageView by bind<ShieldImageView>(R.id.roomAvatarDecorationImageView)
        val roomAvatarPublicDecorationImageView by bind<ImageView>(R.id.roomAvatarPublicDecorationImageView)
        val roomAvatarFailSendingImageView by bind<ImageView>(R.id.roomAvatarFailSendingImageView)
        val roomAvatarPresenceImageView by bind<PresenceStateImageView>(R.id.roomAvatarPresenceImageView)
        val rootView by bind<ViewGroup>(R.id.itemRoomLayout)
    }

    fun format(timelineEvent: TimelineEvent, isDm: Boolean, appendAuthor: Boolean): CharSequence {
        if (timelineEvent.root.isRedacted()) {
            return noticeEventFormatter.formatRedactedEvent(timelineEvent.root)
        }

        if (timelineEvent.root.isEncrypted() &&
                timelineEvent.root.mxDecryptionResult == null) {
            return stringProvider.getString(R.string.encrypted_message)
        }

//        val senderName = timelineEvent.senderInfo.disambiguatedDisplayName
        var senderName = ""
        timelineEvent.senderInfo.userId.getBestNameEachChat(timelineEvent.senderInfo.disambiguatedDisplayName) {
            senderName = it
        }

        return when (timelineEvent.root.getClearType()) {
            EventType.MESSAGE               -> {
                timelineEvent.getLastMessageContent()?.let { messageContent ->
                    when (messageContent.msgType) {
                        MessageType.MSGTYPE_TEXT                 -> {
                            val body = messageContent.getTextDisplayableContent()
                            if (messageContent is MessageTextContent && messageContent.matrixFormattedBody.isNullOrBlank().not()) {
                                val localFormattedBody = htmlRenderer.parse(body) as Document
                                val renderedBody = htmlRenderer.render(localFormattedBody) ?: body
                                simpleFormat(senderName, renderedBody, appendAuthor)
                            } else {
                                simpleFormat(senderName, body, appendAuthor)
                            }
                        }
                        MessageType.MSGTYPE_VERIFICATION_REQUEST -> {
                            simpleFormat(senderName, stringProvider.getString(R.string.verification_request), appendAuthor)
                        }
                        MessageType.MSGTYPE_IMAGE                -> {
                            simpleFormat(senderName, stringProvider.getString(R.string.sent_an_image), appendAuthor)
                        }
                        MessageType.MSGTYPE_AUDIO                -> {
                            if ((messageContent as? MessageAudioContent)?.voiceMessageIndicator != null) {
                                simpleFormat(senderName, stringProvider.getString(R.string.sent_a_voice_message), appendAuthor)
                            } else {
                                simpleFormat(senderName, stringProvider.getString(R.string.sent_an_audio_file), appendAuthor)
                            }
                        }
                        MessageType.MSGTYPE_VIDEO                -> {
                            simpleFormat(senderName, stringProvider.getString(R.string.sent_a_video), appendAuthor)
                        }
                        MessageType.MSGTYPE_FILE                 -> {
                            simpleFormat(senderName, stringProvider.getString(R.string.sent_a_file), appendAuthor)
                        }
                        MessageType.MSGTYPE_LOCATION             -> {
                            simpleFormat(senderName, stringProvider.getString(R.string.sent_location), appendAuthor)
                        }
                        else                                     -> {
                            simpleFormat(senderName, messageContent.body, appendAuthor)
                        }
                    }
                } ?: span { }
            }
            EventType.STICKER               -> {
                simpleFormat(senderName, stringProvider.getString(R.string.send_a_sticker), appendAuthor)
            }
            EventType.REACTION              -> {
                timelineEvent.root.getClearContent().toModel<ReactionContent>()?.relatesTo?.let {
                    val emojiSpanned = emojiSpanify.spanify(stringProvider.getString(R.string.sent_a_reaction, it.key))
                    simpleFormat(senderName, emojiSpanned, appendAuthor)
                } ?: span { }
            }
            EventType.KEY_VERIFICATION_CANCEL,
            EventType.KEY_VERIFICATION_DONE -> {
                // cancel and done can appear in timeline, so should have representation
                simpleFormat(senderName, stringProvider.getString(R.string.sent_verification_conclusion), appendAuthor)
            }
            EventType.KEY_VERIFICATION_START,
            EventType.KEY_VERIFICATION_ACCEPT,
            EventType.KEY_VERIFICATION_MAC,
            EventType.KEY_VERIFICATION_KEY,
            EventType.KEY_VERIFICATION_READY,
            EventType.CALL_CANDIDATES       -> {
                span { }
            }
            EventType.POLL_START            -> {
                timelineEvent.root.getClearContent().toModel<MessagePollContent>(catchError = true)?.pollCreationInfo?.question?.question
                        ?: stringProvider.getString(R.string.sent_a_poll)
            }
            EventType.POLL_RESPONSE         -> {
                stringProvider.getString(R.string.poll_response_room_list_preview)
            }
            EventType.POLL_END              -> {
                stringProvider.getString(R.string.poll_end_room_list_preview)
            }
            else                            -> {
                span {
                    text = noticeEventFormatter.format(timelineEvent, isDm) ?: ""
                    textStyle = "italic"
                }
            }
        }
    }

    private fun simpleFormat(senderName: String, body: CharSequence, appendAuthor: Boolean): CharSequence {
        return if (appendAuthor) {
            span {
                text = senderName
                textColor = colorProvider.getColorFromAttribute(R.attr.vctr_content_primary)
            }
                    .append(": ")
                    .append(body)
        } else {
            body
        }
    }
}
