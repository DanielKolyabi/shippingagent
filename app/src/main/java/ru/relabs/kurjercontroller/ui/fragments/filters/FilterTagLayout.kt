package ru.relabs.kurjercontroller.ui.fragments.filters

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Constraints
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.models.Filter
import ru.relabs.kurjercontroller.ui.extensions.setVisible


class FilterTagLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ViewGroup(context, attrs, defStyleAttr) {
    internal var deviceWidth: Int = 0

    private val tags: MutableList<Pair<Int, Filter>> = mutableListOf()

    init {
        init(context)
    }

    fun add(tag: Filter) {
        val view = LayoutInflater.from(context).inflate(R.layout.item_filter, this, false)
        view.findViewById<TextView>(R.id.text).text = tag.name
        view.findViewById<ImageView>(R.id.close_icon).apply {
            setOnClickListener {
                remove(tag)
            }
            setVisible(!tag.fixed)
        }
        val lpc = (view.layoutParams as? Constraints.LayoutParams)
        lpc?.setMargins(0, 0, 2, 2)
        lpc?.let{
            view.layoutParams = it
        }

        val indexedTag = (tags.maxBy { it.first }?.first?.plus(1) ?: 0) to tag
        tags.add(indexedTag)

        view.tag = indexedTag.first

        addView(view)
    }

    fun remove(tag: Filter) {
        tags.firstOrNull { it.second == tag }?.let {
            removeView(findViewWithTag(it.first))
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
                return

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
        var rowCount = 0

        // Iterate through all childrens, measuring them and computing our dimensions
        // from their size.
        for (i in 0 until count) {
            val child = getChildAt(i)

            if (child.visibility == View.GONE)
                continue

            // Measure the child.
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            maxWidth += Math.max(maxWidth, child.measuredWidth)
            mLeftWidth += child.measuredWidth

            if (mLeftWidth / deviceWidth > rowCount) {
                maxHeight += child.measuredHeight
                rowCount++
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