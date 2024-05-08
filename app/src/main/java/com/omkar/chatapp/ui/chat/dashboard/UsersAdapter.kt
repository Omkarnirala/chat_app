package com.omkar.chatapp.ui.chat.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.UserListBinding
import com.omkar.chatapp.ui.signin.signup.UserFirestore

class UsersAdapter(
    private var users: List<UserFirestore>,
    private val userCallback: UserCallback,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private class UserListViewHolder(inflate: UserListBinding) :
        RecyclerView.ViewHolder(inflate.root) {
        val b: UserListBinding = inflate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserListViewHolder(
            UserListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserListViewHolder) {
            users[position].let { list ->

                holder.b.root.setOnClickListener {
                    userCallback.onUserClickedCallBack(position, list)
                }

                holder.b.textViewName.text = list.displayName ?: list.email
                holder.b.textViewMessage.text = list.lastMessage ?: ""
                holder.b.textViewMessagesTime.text = getTimeAgo(list.lastOnlineTime)
                if (list.isOnline) {
                    holder.b.statusIndicator.setImageResource(R.drawable.online_status)
                } else {
                    holder.b.statusIndicator.setImageResource(R.drawable.offline_status)
                }
                Glide.with(holder.b.imageViewProfile.context).load(list.profileImageUrl)
                    .into(holder.b.imageViewProfile)
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun updateList(result: List<UserFirestore>) {
        this.users = result
        notifyDataSetChanged()
    }

    interface UserCallback {
        fun onUserClickedCallBack(position: Int, user: UserFirestore)
    }

    private fun getTimeAgo(time: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - time
        return when {
            diff < 60000 -> "just now"
            diff < 3600000 -> "${diff / 60000} minutes ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            else -> "${diff / 86400000} days ago"
        }
    }

}
