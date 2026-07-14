package com.nobodysapps.septimanapp.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nobodysapps.septimanapp.databinding.ItemPropositumBinding
import com.nobodysapps.septimanapp.model.Propositum
import java.util.*

/**
 * Shows the proposita as expandable cards. Header (subject + author) is always visible; tapping a
 * card expands its full description. Only one card is expanded at a time.
 */
class PropositaAdapter(
    private var proposita: List<Propositum>,
    private var locale: Locale
) : RecyclerView.Adapter<PropositaAdapter.PropositumViewHolder>() {

    private var expandedPosition = RecyclerView.NO_POSITION

    fun setLocale(newLocale: Locale) {
        locale = newLocale
        notifyItemRangeChanged(0, itemCount)
    }

    fun setProposita(newProposita: List<Propositum>) {
        proposita = newProposita
        expandedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropositumViewHolder {
        val binding = ItemPropositumBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PropositumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PropositumViewHolder, position: Int) {
        holder.bind(proposita[position], position == expandedPosition)
    }

    override fun getItemCount(): Int = proposita.size

    inner class PropositumViewHolder(private val binding: ItemPropositumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val clicked = adapterPosition
                if (clicked == RecyclerView.NO_POSITION) return@setOnClickListener
                val previouslyExpanded = expandedPosition
                expandedPosition = if (clicked == previouslyExpanded) {
                    RecyclerView.NO_POSITION
                } else {
                    clicked
                }
                if (previouslyExpanded != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previouslyExpanded)
                }
                if (expandedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(expandedPosition)
                }
            }
        }

        fun bind(propositum: Propositum, expanded: Boolean) {
            binding.propositumSubject.text = propositum.subjectForLocale(locale)
            binding.propositumAuthor.text = propositum.author
            binding.propositumDescription.text = propositum.descriptionForLocale(locale)
            binding.propositumDescription.visibility =
                if (expanded) android.view.View.VISIBLE else android.view.View.GONE
            binding.propositumExpandIcon.rotation = if (expanded) 180f else 0f
        }
    }
}
