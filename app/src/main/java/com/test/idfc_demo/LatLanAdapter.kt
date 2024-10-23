package com.test.idfc_demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test.idfc_demo.databinding.LatAndLanItemBinding

class LatLanAdapter(var list: MutableList<LatLanItems>): RecyclerView.Adapter<LatLanAdapter.LatLanViewHolder>() {

    private lateinit var binding: LatAndLanItemBinding

    inner class LatLanViewHolder(private val bind: LatAndLanItemBinding) : RecyclerView.ViewHolder(bind.root){
        fun binder(position: Int) {
            bind.lanTV.text = list[position].lan.toString()
            bind.latTV.text = list[position].lat.toString()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatLanViewHolder {
       binding = LatAndLanItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return LatLanViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: LatLanViewHolder, position: Int) {

        holder.binder(position)
    }
    fun updateLatLong( updateList: MutableList<LatLanItems>){
        list = updateList
        notifyDataSetChanged()
    }
}