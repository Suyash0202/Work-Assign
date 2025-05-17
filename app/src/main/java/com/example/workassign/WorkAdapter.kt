package com.example.workassign


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.workassign.databinding.ItemVewWorksfregmentBinding

class WorkAdapter(val onUnassignedClick: (Works) -> Unit) : RecyclerView.Adapter<WorkAdapter.WorkViewHolder>() {

    inner class WorkViewHolder(val binding: ItemVewWorksfregmentBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffUtil = object : DiffUtil.ItemCallback<Works>() {
        override fun areItemsTheSame(oldItem: Works, newItem: Works): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Works, newItem: Works): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkViewHolder {
        val binding = ItemVewWorksfregmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkViewHolder, position: Int) {

        val works = differ.currentList[position]
        val isExpanded = works.expanded
      //  Log.d("WorkAdapter", "List submitted with ${workList.size} items")


        holder.binding.apply {
            tvTitle.text = works.workTitle
            tvDate.text = works.workLastDate
            tvWorkDescription.text = works.workDescription

            when (works.workPriority) {
                "1" -> ivOval.setImageResource(R.drawable.green_oval)
                "2" -> ivOval.setImageResource(R.drawable.yellow_oval)
                "3" -> ivOval.setImageResource(R.drawable.red_oval)
            }

            tvStatus.text = when (works.workStatus) {
                "1" -> "Pending"
                "2" -> "Progress"
                "3" -> "Completed"
                else -> "Cancelled"
            }

            val visibility = if (isExpanded) View.VISIBLE else View.GONE

             tvWorkDescription.visibility = visibility
             btnWorkDone.visibility = visibility
             workDescT.visibility = visibility


            constraintLayout.setOnClickListener {
                isAnyItem(position)
            }

            btnWorkDone.setOnClickListener {
                onUnassignedClick(works)
            }
        }
    }

    private fun isAnyItem(i: Int) {
        val currentList = differ.currentList.map { it.copy() }.toMutableList()
        val expandedItem = currentList.indexOfFirst { it.expanded }

        if (expandedItem >= 0 && expandedItem != i) {
            currentList[expandedItem] = currentList[expandedItem].copy(expanded = false)
        }

        currentList[i] = currentList[i].copy(expanded = !currentList[i].expanded)
        differ.submitList(currentList)
    }

    override fun getItemCount(): Int = differ.currentList.size
}
