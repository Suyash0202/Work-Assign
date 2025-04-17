
package com.example.workassign

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.workassign.Data.Users
import com.example.workassign.databinding.EmployeeProfileItemBinding

class EmployeeAdapter: RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

    class EmployeeViewHolder(val binding: EmployeeProfileItemBinding):ViewHolder(binding.root)
    val diffUtil = object : DiffUtil.ItemCallback<Users>(){

        override fun areItemsTheSame(oldItem: Users, newItem: Users, ): Boolean {
            return oldItem.Id == newItem.Id
        }

        override fun areContentsTheSame(oldItem: Users, newItem: Users, ): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int,): EmployeeViewHolder {
        return EmployeeViewHolder(EmployeeProfileItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int, ) {
        val empData = differ.currentList[position]
        holder.binding.apply {
            Glide.with(holder.itemView)
                .load(empData.image)
                .circleCrop()
                .placeholder(R.drawable.circle_background)
                .into(rvImage)
            rvName.text = empData.name
        }
        holder.itemView.setOnClickListener {
            val action = employeeFragmentDirections.actionEmployeeFragmentToWorkFragment(empData)
            Navigation.findNavController(it).navigate(action)
        } 
    }
    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}
