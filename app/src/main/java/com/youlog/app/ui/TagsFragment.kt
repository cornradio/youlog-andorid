package com.youlog.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.youlog.app.R
import com.youlog.app.data.AppDatabase
import com.youlog.app.data.TagEntity
import com.youlog.app.repository.ImageRepository
import com.youlog.app.ui.viewmodel.MainViewModel
import com.youlog.app.ui.viewmodel.MainViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TagsFragment : Fragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: TagsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val repository = ImageRepository(database.imageDao(), database.tagDao())
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[MainViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.tagsRecyclerView)
        adapter = TagsAdapter { tag ->
            openTagFilter(tag)
        }
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allTags.collectLatest { tags ->
                adapter.submitList(tags)
            }
        }
    }

    private fun openTagFilter(tag: TagEntity) {
        val intent = Intent(requireContext(), TaggedImagesActivity::class.java)
        intent.putExtra("tag_name", tag.name)
        startActivity(intent)
    }

    class TagsAdapter(private val onTagClick: (TagEntity) -> Unit) : RecyclerView.Adapter<TagsAdapter.TagViewHolder>() {
        private var tags = listOf<TagEntity>()

        fun submitList(newTags: List<TagEntity>) {
            tags = newTags
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
            return TagViewHolder(view, onTagClick)
        }

        override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
            holder.bind(tags[position])
        }

        override fun getItemCount(): Int = tags.size

        class TagViewHolder(itemView: View, private val onTagClick: (TagEntity) -> Unit) : RecyclerView.ViewHolder(itemView) {
            private val nameText = itemView.findViewById<TextView>(R.id.tagNameTextView)
            private val countText = itemView.findViewById<TextView>(R.id.tagCountTextView)

            fun bind(tag: TagEntity) {
                nameText.text = tag.name
                countText.text = "${tag.usageCount} 张照片"
                itemView.setOnClickListener { onTagClick(tag) }
            }
        }
    }
}
