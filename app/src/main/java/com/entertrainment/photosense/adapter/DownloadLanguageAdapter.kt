package com.entertrainment.photosense.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.entertrainment.photosense.R
import kotlinx.android.synthetic.main.language_item.view.*

class DownloadLanguageAdapter(private val mLanguageList: Array<Pair<String, Int>>,
                              private val mLanguageClickListener: OnLanguageSelected): RecyclerView.Adapter<DownloadLanguageAdapter.LanguageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.language_item, parent, false))
    }

    override fun getItemCount() = mLanguageList.size

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.tvLanguage.text = mLanguageList[position].first
    }

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLanguage = itemView.tvLanguage

        init {
            itemView.setOnClickListener {
                mLanguageClickListener.onClick(mLanguageList[adapterPosition].first, mLanguageList[adapterPosition].second)
            }
        }
    }

    interface OnLanguageSelected {
        fun onClick(name: String, id: Int)
    }
}