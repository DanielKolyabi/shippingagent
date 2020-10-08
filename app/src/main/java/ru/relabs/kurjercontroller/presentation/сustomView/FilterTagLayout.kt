package ru.relabs.kurjercontroller.presentation.сustomView

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.FilterModel


class FilterTagLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ViewGroup(context, attrs, defStyleAttr) {
    internal var deviceWidth: Int = 0

    private val tags: MutableList<Pair<Int, FilterModel>> = mutableListOf()
    var onFilterAppear: ((filter: FilterModel) -> Unit)? = null
    var onFilterDisappear: ((filter: FilterModel) -> Unit)? = null
    var onFilterActiveChangedPredicate: ((filter: FilterModel, newActiveState: Boolean) -> Boolean)? = null
    var onFilterActiveChanged: ((filter: FilterModel) -> Unit)? = null


    init {
        init(context)
    }

    private fun changeTagIcon(view: ImageView, tag: FilterModel){
        val drawable = if(tag.fixed){
            if(tag.active) {
                context.getDrawable(R.drawable.ic_filter_active)
            }else{
                context.getDrawable(R.drawable.ic_filter_not_active)
            }
        }else{
            context.getDrawable(R.drawable.ic_remove_filter)
        }
        view.setImageDrawable(drawable)
    }

    private fun bindTagControl(view: ImageView, tag: FilterModel){
        if(tag.fixed){
            view.setOnClickListener {
                if(onFilterActiveChangedPredicate?.invoke(tag, !tag.active) == true) {
                    tag.active = !tag.active
                    changeTagIcon(view, tag)
                    onFilterActiveChanged?.invoke(tag)
                }
            }
        }else{
            view.setOnClickListener {
                remove(tag)
            }
        }
    }

    fun add(tag: FilterModel) {
        val view = LayoutInflater.from(context).inflate(R.layout.item_filter, this, false)
        view.findViewById<TextView>(R.id.text).text = tag.name
        view.findViewById<ImageView>(R.id.close_icon).apply {
            changeTagIcon(this, tag)
            bindTagControl(this, tag)
        }
        val lpc = (view.layoutParams as? LinearLayout.LayoutParams)
        lpc?.setMargins(2, 2, 2, 2)
        lpc?.let {
            view.layoutParams = it
        }

        val indexedTag = (tags.maxBy { it.first }?.first?.plus(1) ?: 0) to tag
        tags.add(indexedTag)

        view.tag = indexedTag.first

        onFilterAppear?.invoke(tag)
        addView(view)

    }

    fun remove(tag: FilterModel) {
        tags.firstOrNull { it.second == tag }?.let {
            removeView(findViewWithTag(it.first))
            onFilterDisappear?.invoke(it.second)
            tags.remove(it)
        }
    }

    private fun init(context: Context) {
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val deviceDisplay = Point()
        display.getSize(deviceDisplay)
        deviceWidth = deviceDisplay.x
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        var curWidth: Int
        var curHeight: Int
        var curLeft: Int
        var curTop: Int
        var maxHeight: Int

        //get the available size of child view
        val childLeft = this.paddingLeft
        val childTop = this.paddingTop
        val childRight = this.measuredWidth - this.paddingRight
        val childBottom = this.measuredHeight - this.paddingBottom
        val childWidth = childRight - childLeft
        val childHeight = childBottom - childTop

        maxHeight = 0
        curLeft = childLeft
        curTop = childTop

        for (i in 0 until count) {
            val child = getChildAt(i)

            if (child.visibility == View.GONE)
                continue

            //Get the maximum size of the child
            child.measure(
                MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST)
            )
            curWidth = child.measuredWidth
            curHeight = child.measuredHeight
            //wrap is reach to the end
            if (curLeft + curWidth >= childRight) {
                curLeft = childLeft
                curTop += maxHeight
                maxHeight = 0
            }
            //do the layout
            child.layout(curLeft, curTop, curLeft + curWidth, curTop + curHeight)
            //store the max height
            if (maxHeight < curHeight)
                maxHeight = curHeight
            curLeft += curWidth
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = childCount
        // Measurement will ultimately be computing these values.
        var maxHeight = 0
        var maxWidth = 0
        var childState = 0
        var mLeftWidth = 0

        // Iterate through all childrens, measuring them and computing our dimensions
        // from their size.
        for (i in 0 until count) {
            val child = getChildAt(i)

            if (child.visibility == View.GONE)
                continue

            // Measure the child.
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            mLeftWidth += child.measuredWidth
            maxWidth = Math.max(maxWidth, mLeftWidth)

            if (mLeftWidth > deviceWidth*0.9) {
                maxHeight += child.measuredHeight
                mLeftWidth = child.measuredWidth
            } else {
                maxHeight = Math.max(maxHeight, child.measuredHeight)
            }
            childState = View.combineMeasuredStates(childState, child.measuredState)
        }

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, suggestedMinimumHeight)
        maxWidth = Math.max(maxWidth, suggestedMinimumWidth)

        // Report our final dimensions.
        setMeasuredDimension(
            View.resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            View.resolveSizeAndState(maxHeight, heightMeasureSpec, childState shl View.MEASURED_HEIGHT_STATE_SHIFT)
        )
    }
}