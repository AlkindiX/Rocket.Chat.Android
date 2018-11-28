package chat.rocket.android.chatdetails.presentation

import chat.rocket.android.chatdetails.domain.ChatDetails
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.getInfo
import chat.rocket.core.model.Room
import javax.inject.Inject

class ChatDetailsPresenter @Inject constructor(
    private val view: ChatDetailsView,
    private val navigator: ChatDetailsNavigator,
    private val strategy: CancelStrategy,
    serverInteractor: GetCurrentServerInteractor,
    factory: ConnectionManagerFactory
) {
    private val currentServer = serverInteractor.get()!!
    private val manager = factory.create(currentServer)
    private val client = manager.client

    fun getDetails(chatRoomId: String, chatRoomType: String) {
        launchUI(strategy) {
            try {
                val room = retryIO("getInfo($chatRoomId, null, $chatRoomType") {
                    client.getInfo(chatRoomId, null, roomTypeOf(chatRoomType))
                }

                view.displayDetails(roomToChatDetails(room))
            } catch(e: Exception) {
                e.message.let {
                    view.showMessage(it!!)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun toFiles(chatRoomId: String) {
        navigator.toFileList(chatRoomId)
    }

    fun toMembers(chatRoomId: String) {
        navigator.toMembersList(chatRoomId)
    }

    fun toMentions(chatRoomId: String) {
        navigator.toMentions(chatRoomId)
    }

    fun toPinned(chatRoomId: String) {
        navigator.toPinnedMessageList(chatRoomId)
    }

    fun toFavorites(chatRoomId: String) {
        navigator.toFavoriteMessageList(chatRoomId)
    }

    private fun roomToChatDetails(room: Room): ChatDetails {
        return with(room) {
            ChatDetails(
                name = name,
                fullName = fullName,
                type = type.toString(),
                topic = topic,
                description = description,
                announcement = announcement
            )
        }
    }
}