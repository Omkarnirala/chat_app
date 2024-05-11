package com.omkar.chatapp.ui.chat.messaging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.omkar.chatapp.databinding.MessageBinding
import com.omkar.chatapp.utils.FirebaseUtil
import com.omkar.chatapp.utils.FirebaseUtil.timestampToString
import com.omkar.chatapp.utils.formatTimestampTo12Hour
import com.omkar.chatapp.utils.log
import java.text.SimpleDateFormat
import java.util.Locale

class MessagesAdapter(
    options: FirestoreRecyclerOptions<ChatMessageModel>,
) : FirestoreRecyclerAdapter<ChatMessageModel, MessagesAdapter.MessageListViewHolder>(options) {

    companion object {
        private const val mTag: String = "MessagesAdapter"
    }

    class MessageListViewHolder(message: MessageBinding) : RecyclerView.ViewHolder(message.root) {
        val b: MessageBinding = message
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageListViewHolder {
        return MessageListViewHolder(
            MessageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MessageListViewHolder, position: Int, model: ChatMessageModel) {
        log(mTag, "Message: $model")
        if (model.senderID == FirebaseUtil.currentUserId()) {
            holder.b.receiverRootLayout.visibility = View.GONE
            holder.b.senderRootLayout.visibility = View.VISIBLE
            holder.b.senderMessageText.text = model.message
            holder.b.senderMessageTime.text = timestampToString(model.timeStamps!!)
        } else {
            holder.b.senderRootLayout.visibility = View.GONE
            holder.b.receiverRootLayout.visibility = View.VISIBLE
            holder.b.receiverMessageText.text = model.message
            holder.b.receiverMessageTime.text = timestampToString(model.timeStamps!!)

        }
    }

}