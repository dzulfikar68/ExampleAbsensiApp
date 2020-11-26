package io.github.dzulfikar68.exampleabsensiapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(
        private val items: List<UserItem>
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName?.text = item.name
        holder.tvAbsen?.text = item.absen
        holder.tvTimeAndDate?.text = "Time = ${item.date}"
    }

    inner class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        var tvName: TextView? = null
        var tvAbsen: TextView? = null
        var tvTimeAndDate: TextView? = null

        init {
            tvName = row.findViewById(R.id.tvName) as TextView
            tvAbsen = row.findViewById(R.id.tvAbsen) as TextView
            tvTimeAndDate = row.findViewById(R.id.tvTimeAndDate) as TextView
        }
    }
}