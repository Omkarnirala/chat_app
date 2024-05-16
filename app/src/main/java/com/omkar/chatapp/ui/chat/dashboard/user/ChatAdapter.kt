package com.omkar.chatapp.ui.chat.dashboard.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.UserListBinding
import com.omkar.chatapp.ui.chat.messaging.ChatRoomModel
import com.omkar.chatapp.ui.signin.signup.UserDetailsModel
import com.omkar.chatapp.utils.FirebaseUtil
import com.omkar.chatapp.utils.getTimeAgo
import com.omkar.chatapp.utils.log

class ChatAdapter(
    options: FirestoreRecyclerOptions<ChatRoomModel>,
    private val userCallback: UserCallback,
) : FirestoreRecyclerAdapter<ChatRoomModel, ChatAdapter.UserListViewHolder>(options) {

    private val mTag: String = "ChatAdapter"

    class UserListViewHolder(inflate: UserListBinding) : RecyclerView.ViewHolder(inflate.root) {
        val b: UserListBinding = inflate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        return UserListViewHolder(
            UserListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int, model: ChatRoomModel) {
        FirebaseUtil.getOtherUserFromChatroom(model.userIds).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    log(mTag, "ChatRoomModel: ${task.result}")
                    val lastMessageSentByMe: Boolean = model.lastMessageSenderId.equals(FirebaseUtil.currentUserId())
                    val otherUserModel: UserDetailsModel? = task.result.toObject(UserDetailsModel::class.java)
                    log(mTag, "UserDetailsModel $otherUserModel")
                    holder.b.textViewName.text = otherUserModel?.displayName ?: otherUserModel?.email
                    if (lastMessageSentByMe) {
                        holder.b.textViewMessage.text = holder.itemView.context.getString(R.string.your_message, model.lastMessage)
                    } else {
                        holder.b.textViewMessage.text = model.lastMessage
                    }
                    Glide.with(holder.b.imageViewProfile.context).load(otherUserModel?.profileImageUrl ?: R.drawable.ic_profile)
                        .apply(RequestOptions().circleCrop()).into(
                            holder.b.imageViewProfile
                        )
                    holder.b.textViewMessagesTime.text = getTimeAgo(otherUserModel!!.lastOnlineTime)

                    holder.b.root.setOnClickListener {
                        userCallback.onUserClickedCallBack(position, otherUserModel)
                    }
                }
            }
    }

    interface UserCallback {
        fun onUserClickedCallBack(position: Int, user: UserDetailsModel?)
    }


}