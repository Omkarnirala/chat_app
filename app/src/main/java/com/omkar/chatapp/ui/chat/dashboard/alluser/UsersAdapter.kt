package com.omkar.chatapp.ui.chat.dashboard.alluser

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.UserListBinding
import com.omkar.chatapp.ui.signin.signup.UserDetailsModel
import com.omkar.chatapp.utils.getTimeAgo
import com.omkar.chatapp.utils.log

class UsersAdapter(
    options: FirestoreRecyclerOptions<UserDetailsModel>,
    private val userCallback: UserCallback,
) : FirestoreRecyclerAdapter<UserDetailsModel, UsersAdapter.UserListViewHolder>(options) {

    private val mTag: String = "UsersAdapter"

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

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int, model: UserDetailsModel) {
        holder.b.root.setOnClickListener {
            userCallback.onUserClickedCallBack(position, model)
        }
        log(mTag, "model $model")
        holder.b.textViewName.text = model.displayName ?: model.email
        holder.b.textViewMessage.text = model.lastMessage ?: "Status: ${model.status}"
        holder.b.textViewMessagesTime.text = getTimeAgo(model.lastOnlineTime)
        Glide.with(holder.b.imageViewProfile.context).load(model.profileImageUrl ?: R.drawable.ic_profile).apply(RequestOptions().circleCrop())
            .into(holder.b.imageViewProfile)
    }

    interface UserCallback {
        fun onUserClickedCallBack(position: Int, user: UserDetailsModel?)
    }

}
