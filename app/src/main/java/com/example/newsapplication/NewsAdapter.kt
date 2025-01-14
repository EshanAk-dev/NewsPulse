package com.example.newsapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapplication.editor.NewsDetailActivity
import com.example.newsapplication.user.UserNewsDetailsActivity

class NewsAdapter(
    private val newsList: List<News>,
    private val userRole: String,  // Added userRole parameter
    private val onItemClick: (News) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.bind(news)
    }

    override fun getItemCount(): Int = newsList.size

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_Title)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_Category)
        private val imgPreview: ImageView = itemView.findViewById(R.id.img_Preview)

        fun bind(news: News) {
            tvTitle.text = news.title
            tvCategory.text = news.category

            // Load image using Glide
            Glide.with(itemView.context)
                .load(news.imageUrl)
                .into(imgPreview)

            itemView.setOnClickListener {
                // Open the appropriate news detail activity based on the user role
                val intent = if (userRole == "editor") {
                    Intent(itemView.context, NewsDetailActivity::class.java)
                } else {
                    Intent(itemView.context, UserNewsDetailsActivity::class.java)
                }
                intent.putExtra("news", news) // Pass the news object
                itemView.context.startActivity(intent)
            }
        }
    }
}
