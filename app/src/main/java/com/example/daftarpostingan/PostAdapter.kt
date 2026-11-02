package com.example.daftarpostingan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.daftarpostingan.data.Post

class PostAdapter(
    private val onKlik: (Post) -> Unit,
    private val onHapus: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val listPost = mutableListOf<Post>()

    fun tampilkan(posts: List<Post>) {
        listPost.clear()
        listPost.addAll(posts)
        notifyDataSetChanged()
    }

    fun hapusPada(posisi: Int) {
        if (posisi in listPost.indices) {
            listPost.removeAt(posisi)
            notifyItemRemoved(posisi)
        }
    }

    fun posisiDari(post: Post): Int {
        return listPost.indexOfFirst { it.id == post.id }
    }

    val totalItem: Int
        get() = listPost.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(listPost[position])
    }

    override fun getItemCount(): Int = listPost.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)

        fun bind(post: Post) {
            tvTitle.text = post.title
            tvSubtitle.text = itemView.context.getString(
                R.string.post_subtitle_format,
                post.id,
                post.userId
            )

            itemView.setOnClickListener {
                onKlik(post)
            }

            itemView.setOnLongClickListener {
                onHapus(post)
                true
            }
        }
    }
}