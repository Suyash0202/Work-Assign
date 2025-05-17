package com.example.workassign

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.workassign.databinding.EmployeeProfileItemBinding
import com.example.workassign.databinding.ItemViewEmployeeWorksBinding
import com.google.android.material.button.MaterialButton

class EmployeeActivityAdapter( val onProgressClick: (Works, MaterialButton) -> Unit,
                               val onCompleteClick: (Works, MaterialButton) -> Unit): RecyclerView.Adapter<EmployeeActivityAdapter.EmployeeActivityViewHolder>() {

    class EmployeeActivityViewHolder(val binding: ItemViewEmployeeWorksBinding): RecyclerView.ViewHolder(binding.root)

    private val diffUtil = object: DiffUtil.ItemCallback<Works>(){
        override fun areItemsTheSame(oldItem: Works, newItem: Works, ): Boolean {
            return oldItem.id ==newItem.id
        }

        override fun areContentsTheSame(oldItem: Works, newItem: Works, ): Boolean {
            return oldItem== newItem
        }

    }
     val differ = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, ): EmployeeActivityViewHolder {
        return EmployeeActivityViewHolder(ItemViewEmployeeWorksBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: EmployeeActivityViewHolder, position: Int, ) {
       val work = differ.currentList[position]
        var isExpanded = work.expanded
        holder.binding.apply{
            tvTitle.text = work.workTitle
             tvDate.text= work.workLastDate
             tvWorkDescription.text=work.workDescription

            when(work.workPriority){
                "1"->ivOval.setImageResource(R.drawable.green_oval)
                "2"->ivOval.setImageResource(R.drawable.yellow_oval)
                "3"->ivOval.setImageResource(R.drawable.red_oval)
                else->ivOval.setImageResource(R.drawable.green_oval)
            }
            when(work.workStatus){
                "1" -> {
                    tvStatus.text = "Pending"
                    btnWorkStart.isEnabled = true
                    btnWorkDone.isEnabled = false
                    btnWorkStart.text = "Start"
                }
                "2" -> {
                    tvStatus.text = "Progress"
                    btnWorkStart.isEnabled = false
                    btnWorkDone.isEnabled = true
                    btnWorkStart.text = "In Progress"
                }
                "3" -> {
                    tvStatus.text = "Completed"
                    btnWorkStart.isEnabled = false
                    btnWorkDone.isEnabled = false
                    btnWorkStart.text = "In Progress"
                    btnWorkDone.text = "Completed"
                }
            }

            tvWorkDescription.visibility = if (isExpanded) View.VISIBLE else View.GONE
            btnWorkStart.visibility= if(isExpanded) View.VISIBLE else View.GONE
            btnWorkDone.visibility= if (isExpanded) View.VISIBLE else View.GONE
            workDescT.visibility= if (isExpanded) View.VISIBLE else View.GONE
            constraintLayout.setOnClickListener {
                toggleExpand(position)
            }

            btnWorkStart.setOnClickListener {onProgressClick(work,btnWorkStart)}
            btnWorkDone.setOnClickListener {onCompleteClick(work,btnWorkDone)}

        }
    }

    private fun toggleExpand(position: Int) {
        val currentList = differ.currentList.toMutableList()
        val updatedList = currentList.mapIndexed { index, item ->
            if (index == position) item.copy(expanded = !item.expanded)
            else item.copy(expanded = false)
        }
        differ.submitList(updatedList)
    }






    override fun getItemCount(): Int {
       return differ.currentList.size
    }




}