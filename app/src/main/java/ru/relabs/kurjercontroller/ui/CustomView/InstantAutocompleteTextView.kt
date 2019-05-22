package ru.relabs.kurjercontroller.ui.CustomView

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.AutoCompleteTextView
import ru.relabs.kurjercontroller.R

/**
 * Created by ProOrange on 21.05.2019.
 */
class InstantAutocompleteTextView(
    ctx: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : AutoCompleteTextView(ctx, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.autoCompleteTextViewStyle)
    constructor(context: Context) : this(context, null)

    override fun enoughToFilter(): Boolean {
        return true
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if(focused && filter != null){
            performFiltering(text, 0)
        }
    }
}