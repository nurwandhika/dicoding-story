package com.example.projectbangkit1.ui.adapter


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectbangkit1.R
import com.example.projectbangkit1.data.response.ListStoryItem
import com.example.projectbangkit1.databinding.ListLayoutBinding
import com.example.projectbangkit1.ui.screen.DetailActivity

class ListItemAdapter : PagingDataAdapter<ListStoryItem, ListItemAdapter.ListViewHolder>(DIFF_CALLBACK) {

    class ListViewHolder (private var binding: ListLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: ListStoryItem) {
            binding.apply {
                itemView.context.let { ic ->
                    judulList.text = user.name
                    Glide.with(ic)
                        .load(user.photoUrl)
                        .into(imageList)
                    overviewList.text = ic.getString(R.string.story, user.description)
                    itemView.setOnClickListener {
                        val intent = Intent(ic, DetailActivity::class.java)
                        intent.putExtra(STORYKEY, user.id)
                        val optionsCompat: ActivityOptionsCompat =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                ic as Activity,
                                Pair(imageList, "image"),
                                Pair(judulList, "name"),
                                Pair(overviewList, "desc"),
                            )
                        ic.startActivity(intent, optionsCompat.toBundle())
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val user = getItem(position)
        if (user != null) {
            holder.bind(user)
        }
    }

    companion object {
        const val STORYKEY = "storyid"
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ListStoryItem> =
            object : DiffUtil.ItemCallback<ListStoryItem>() {
                override fun areItemsTheSame(oldUser: ListStoryItem, newUser: ListStoryItem): Boolean {
                    return oldUser == newUser
                }

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(oldUser: ListStoryItem, newUser: ListStoryItem): Boolean {
                    return oldUser.id == newUser.id
                } }
    }
}