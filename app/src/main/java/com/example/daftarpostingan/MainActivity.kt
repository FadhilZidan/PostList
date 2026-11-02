package com.example.daftarpostingan

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.daftarpostingan.data.Post
import com.example.daftarpostingan.network.RestClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutError: View
    private lateinit var tvError: TextView
    private lateinit var btnRetry: Button
    private lateinit var rvPosts: RecyclerView

    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val headerContainer = findViewById<View>(R.id.headerContainer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerContainer.setPadding(
                20.dpToPx(),
                systemBars.top + 16.dpToPx(),
                20.dpToPx(),
                16.dpToPx()
            )
            insets
        }

        initViews()
        setupRecyclerView()
        muatPosts()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        layoutError = findViewById(R.id.layoutError)
        tvError = findViewById(R.id.tvError)
        btnRetry = findViewById(R.id.btnRetry)
        rvPosts = findViewById(R.id.rvPosts)

        btnRetry.setOnClickListener {
            muatPosts()
        }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            onKlik = { post ->
                tampilkanDialogDetail(post)
            },
            onHapus = { post ->
                tampilkanDialogHapus(post)
            }
        )

        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = postAdapter
    }

    private fun muatPosts() {
        progressBar.visibility = View.VISIBLE
        layoutError.visibility = View.GONE
        rvPosts.visibility = View.GONE
        tvStatus.text = getString(R.string.loading_posts)

        lifecycleScope.launch {
            try {
                val posts = RestClient.api.getPosts(limit = 20)
                postAdapter.tampilkan(posts)
                tvStatus.text = getString(R.string.posts_loaded, postAdapter.totalItem)
                progressBar.visibility = View.GONE
                rvPosts.visibility = View.VISIBLE
            } catch (_: IOException) {
                progressBar.visibility = View.GONE
                layoutError.visibility = View.VISIBLE
                tvError.text = getString(R.string.error_loading)
            } catch (_: HttpException) {
                progressBar.visibility = View.GONE
                layoutError.visibility = View.VISIBLE
                tvError.text = getString(R.string.error_loading)
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                layoutError.visibility = View.VISIBLE
                tvError.text = e.localizedMessage ?: getString(R.string.error_loading)
            }
        }
    }

    private fun tampilkanDialogDetail(post: Post) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_detail_post)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvDetailTitle = dialog.findViewById<TextView>(R.id.tvDetailTitle)
        val tvDetailBody = dialog.findViewById<TextView>(R.id.tvDetailBody)
        val btnTutup = dialog.findViewById<TextView>(R.id.btnTutup)

        tvDetailTitle.text = post.title
        tvDetailBody.text = post.body

        btnTutup.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun tampilkanDialogHapus(post: Post) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_delete)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvDeleteMessage = dialog.findViewById<TextView>(R.id.tvDeleteMessage)
        val btnBatal = dialog.findViewById<TextView>(R.id.btnBatal)
        val btnHapus = dialog.findViewById<TextView>(R.id.btnHapus)

        tvDeleteMessage.text = getString(R.string.delete_confirm_message, post.id)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnHapus.setOnClickListener {
            dialog.dismiss()
            hapusPost(post)
        }

        dialog.show()
    }

    private fun hapusPost(post: Post) {
        lifecycleScope.launch {
            try {
                val response = RestClient.api.hapusPost(post.id)
                if (response.isSuccessful) {
                    val posisi = postAdapter.posisiDari(post)
                    if (posisi != -1) {
                        postAdapter.hapusPada(posisi)
                        tvStatus.text = getString(R.string.posts_loaded, postAdapter.totalItem)
                        Toast.makeText(
                            this@MainActivity,
                            "Post #${post.id} berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Gagal menghapus postingan (HTTP ${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Gagal menghapus postingan: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}