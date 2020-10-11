package ru.relabs.kurjercontroller.presentation.host

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.BaseDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.Badgeable
import kotlinx.android.synthetic.main.holder_nav_drawer_item.view.*
import ru.relabs.kurjercontroller.R

/**
 * Created by Daniil Kurchanov on 27.03.2020.
 */
class MenuDrawerItem(override val type: Int) :
    BaseDrawerItem<MenuDrawerItem, RecyclerView.ViewHolder>(), Badgeable<MenuDrawerItem> {

    override fun bindView(holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        val textRes = name?.textRes
        val text = name?.text
        when {
            textRes != null && textRes != -1 -> holder.itemView.tv_title.text = holder.itemView.resources.getText(textRes)
            text != null && text.isNotBlank() -> holder.itemView.tv_title.text = text
        }
//        holder.itemView.selected_overlay.visible = isSelected
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun getViewHolder(v: View): RecyclerView.ViewHolder {
        return ViewHolder(v)
    }

    override val layoutRes: Int
        get() = R.layout.holder_nav_drawer_item

    override var badge: StringHolder? = null

    override fun withBadge(badge: StringHolder?): MenuDrawerItem {
        this.badge = badge
        return this
    }

    override fun withBadge(badgeRes: Int): MenuDrawerItem {
        this.badge = StringHolder(badgeRes)
        return this
    }

    override fun withBadge(badge: String): MenuDrawerItem {
        this.badge = StringHolder(badge)
        return this
    }
}