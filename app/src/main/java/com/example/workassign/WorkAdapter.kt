package com.example.workassign

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.workassign.databinding.ItemVewWorksfregmentBinding

class WorkAdapter(val onUnassignedClick: (Works) -> Unit) : RecyclerView.Adapter<WorkAdapter.WorkViewHolder>() {

    class WorkViewHolder(val binding: ItemVewWorksfregmentBinding):ViewHolder(binding.root)
    val diffUtil = object : DiffUtil.ItemCallback<Works>(){

        override fun areItemsTheSame(oldItem: Works, newItem: Works, ): Boolean {
            return oldItem.Id == newItem.Id
        }

        override fun areContentsTheSame(oldItem: Works, newItem: Works, ): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int,): WorkViewHolder {
        return WorkViewHolder(ItemVewWorksfregmentBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: WorkViewHolder, position: Int, ) {
        val works = differ.currentList[position]
        val isExpanded = works.expanded
        holder.binding.apply{
            tvTitle.text = works.workTitle
            tvDate.text= works.workLastDate
            tvWorkDescription.text=works.workDescription

            when(works.workPriority){
                "1"->ivOval.setImageResource(R.drawable.green_oval)
                "2"->ivOval.setImageResource(R.drawable.yellow_oval)
                "3"->ivOval.setImageResource(R.drawable.red_oval)

            }
            when(works.workStatus){
                "1"->holder.binding.tvStatus.text="Pending"
                "2"->holder.binding.tvStatus.text="Progress"
                "3"->holder.binding.tvStatus.text="Cancelled"
                else->holder.binding.tvStatus.text="Completed"
            }
            tvWorkDescription.visibility = if (isExpanded) View.VISIBLE else View.GONE
            btnWorkDone.visibility= if (isExpanded) View.VISIBLE else View.GONE
            workDescT.visibility= if (isExpanded) View.VISIBLE else View.GONE
            constraintLayout.setOnClickListener {
                isAnyItem(position)
            }
            btnWorkDone.setOnClickListener {onUnassignedClick(works)}


        }
    }

    private fun isAnyItem(i: Int) {
        val currentList = differ.currentList.map { it.copy() }.toMutableList()
        val expandedItem = differ.currentList.indexOfFirst { it.expanded }
        if(expandedItem >= 0 && expandedItem != i){
            differ.currentList[expandedItem].expanded = false
            notifyItemChanged(expandedItem,0)
            notifyItemChanged(i,1)
        }

        currentList[i].expanded = !currentList[i].expanded
        differ.submitList(currentList)


    }
    override fun onBindViewHolder(holder: WorkViewHolder, position: Int, payloads: MutableList<Any>) {
        if(payloads.isNotEmpty() && payloads[0] == 0){
            holder.binding.tvWorkDescription.visibility = View.GONE
            holder.binding.btnWorkDone.visibility= View.GONE
            holder.binding.workDescT.visibility= View.GONE

        }
        else{
            super.onBindViewHolder(holder, position, payloads)
        }


    }



    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}