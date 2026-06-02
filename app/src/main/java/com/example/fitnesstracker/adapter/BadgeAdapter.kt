package com.example.fitnesstracker.adapter

import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.R

data class BadgeModel(val type: String, val name: String, var isEarned: Boolean = false)

class BadgeAdapter : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    private val badges = listOf(
        BadgeModel("1000_STEPS", "1k Steps"),
        BadgeModel("5000_STEPS", "5k Steps"),
        BadgeModel("10000_STEPS", "10k Steps")
    )
    private var earnedBadges = setOf<String>()

    fun updateEarnedBadges(newEarnedBadges: List<String>, onNewBadgeUnlocked: ((String) -> Unit)? = null) {
        val oldEarned = earnedBadges
        earnedBadges = newEarnedBadges.toSet()
        
        badges.forEachIndexed { index, badge ->
            val wasEarned = oldEarned.contains(badge.type)
            val isEarnedNow = earnedBadges.contains(badge.type)
            
            if (!wasEarned && isEarnedNow) {
                // newly unlocked
                badge.isEarned = true
                onNewBadgeUnlocked?.invoke(badge.name)
                notifyItemChanged(index, "ANIMATE")
            } else if (badge.isEarned != isEarnedNow) {
                badge.isEarned = isEarnedNow
                notifyItemChanged(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.bind(badges[position])
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains("ANIMATE")) {
            holder.bind(badges[position])
            holder.animateUnlock()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int = badges.size

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cvBadge: CardView = itemView.findViewById(R.id.cvBadge)
        private val ivBadgeIcon: ImageView = itemView.findViewById(R.id.ivBadgeIcon)
        private val tvBadgeName: TextView = itemView.findViewById(R.id.tvBadgeName)

        fun bind(badge: BadgeModel) {
            tvBadgeName.text = badge.name
            if (badge.isEarned) {
                cvBadge.setCardBackgroundColor(Color.parseColor("#FFD700")) // Gold
                ivBadgeIcon.setImageResource(android.R.drawable.star_on)
            } else {
                cvBadge.setCardBackgroundColor(Color.parseColor("#CCCCCC")) // Grey
                ivBadgeIcon.setImageResource(android.R.drawable.star_off)
            }
        }

        fun animateUnlock() {
            val scaleX = ObjectAnimator.ofFloat(cvBadge, "scaleX", 1.0f, 1.2f, 1.0f)
            val scaleY = ObjectAnimator.ofFloat(cvBadge, "scaleY", 1.0f, 1.2f, 1.0f)
            scaleX.duration = 500
            scaleY.duration = 500
            scaleX.start()
            scaleY.start()
        }
    }
}
