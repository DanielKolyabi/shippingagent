package ru.relabs.kurjercontroller.presentation.addresses

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.presentation.base.DefaultListDiffCallback
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.renderT
import ru.relabs.kurjercontroller.presentation.helpers.TaskAddressSorter
import ru.relabs.kurjercontroller.utils.SearchUtils
import ru.relabs.kurjercontroller.utils.extensions.getColorCompat
import ru.relabs.kurjercontroller.utils.extensions.visible

/**
 * Created by Daniil Kurchanov on 06.04.2020.
 */
object AddressesRenders {
    fun renderLoading(view: View): AddressesRender = renderT(
        { it.loaders > 0 && it.tasks.isNotEmpty() },
        { view.visible = it }
    )

    fun renderCloseButton(view: View): AddressesRender = renderT(
        { it.tasks.size == 1 },
        { view.visible = it }
    )

    fun renderList(adapter: DelegateAdapter<AddressesItem>): AddressesRender = renderT(
        { Triple(it.tasks, it.sorting, it.loaders > 0) to it.searchFilter },
        { (data, searchFilter) ->
            val (tasks, sorting, loading) = data
            val filteredTasks = getSortedTasks(tasks, sorting, searchFilter)
            val allTaskItemsCount = tasks.map { it.taskItems.size }.sum()
            val filteredTaskItemsCount = if (searchFilter.isNotEmpty()) {
                tasks.map { it.taskItems.filter { ti -> SearchUtils.isMatches(ti.address.name, searchFilter) }.size }.sum()
            } else {
                allTaskItemsCount
            }

            val newItems = listOfNotNull(
                AddressesItem.Sorting(sorting, tasks.size == 1 && tasks.firstOrNull()?.filtered == true)
                    .takeIf { tasks.size == 1 },
                AddressesItem.Search(searchFilter)
                    .takeIf { tasks.isNotEmpty() },
                AddressesItem.Loading
                    .takeIf { tasks.isEmpty() && loading }
            ) +
                    filteredTasks +
                    listOfNotNull(
                        AddressesItem.OtherAddresses(allTaskItemsCount - filteredTaskItemsCount)
                            .takeIf { searchFilter.isNotEmpty() },
                        AddressesItem.Blank(tasks.size == 1)
                            .takeIf { tasks.isNotEmpty() }
                    )

            val diff = DiffUtil.calculateDiff(DefaultListDiffCallback(adapter.items, newItems) { o, n ->
                if ((o is AddressesItem.Search && n is AddressesItem.Search && searchFilter.isNotEmpty()) || (o is AddressesItem.Sorting && n is AddressesItem.Sorting && o.sorting == n.sorting)) {
                    true
                } else {
                    null
                }
            })

            adapter.items.clear()
            adapter.items.addAll(newItems)
            diff.dispatchUpdatesTo(adapter)
        }
    )

    fun renderTargetListAddress(adapter: DelegateAdapter<AddressesItem>, list: RecyclerView): AddressesRender = renderT(
        { it.selectedListAddress },
        {
            it?.let { a ->
                adapter.items
                    .indexOfFirst { item -> item is AddressesItem.AddressItem && item.taskItem.address.id == a.id }
                    .takeIf { idx -> idx > 0 }
                    ?.let { idx ->
                        list.scrollToPosition(idx)
                        list.post {
                            list?.findViewHolderForAdapterPosition(idx)?.itemView?.let {
                                flashSelectedColor(it)
                            }
                        }
                    }
            }
        }
    )

    private fun flashSelectedColor(itemView: View) {
        startFlashValueAnimator(itemView) {
            startFlashValueAnimator(itemView)
        }
    }

    private fun startFlashValueAnimator(view: View, onAnimationEnd: (() -> Unit)? = null) {
        val targetColor = view.resources.getColorCompat(R.color.colorAccent)
        val colorFrom = ColorUtils.setAlphaComponent(targetColor, 0)

        val colorAnimationTo = getValueAnimator(colorFrom, targetColor, view, 500)
        val colorAnimationFrom = getValueAnimator(targetColor, colorFrom, view, 500)
        onAnimationEnd?.let {
            colorAnimationFrom.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    it()
                }
            })
        }
        colorAnimationTo.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                colorAnimationFrom.start()
            }
        })

        colorAnimationTo.start()
    }

    private fun getValueAnimator(from: Int, to: Int, view: View?, duration: Int = 250): ValueAnimator {
        return ValueAnimator.ofObject(ArgbEvaluator(), from, to).apply {
            setDuration(duration.toLong())
            addUpdateListener { animator -> view?.setBackgroundColor(animator.animatedValue as Int) }
        }
    }

    private fun getSortedTasks(tasks: List<Task>, sorting: AddressesSortingMethod, searchFilter: String): List<AddressesItem> {
        if (tasks.isEmpty()) {
            return emptyList()
        }

        val taskItems = tasks.flatMap { task -> task.taskItems.map { item -> task to item } }.let { items ->
            if (searchFilter.isNotEmpty()) {
                items.filter { item -> SearchUtils.isMatches(item.second.address.name, searchFilter) }
            } else {
                items
            }
        }

        if (taskItems.isEmpty()) {
            return emptyList()
        }

        val sortedItems = when (sorting) {
            AddressesSortingMethod.STANDARD -> TaskAddressSorter.sortTaskItemsStandart(taskItems)
            AddressesSortingMethod.ALPHABETIC -> TaskAddressSorter.sortTaskItemsAlphabetic(taskItems)
            AddressesSortingMethod.CLOSE_TIME -> TaskAddressSorter.sortTaskItemsCloseTime(taskItems)
        }

        val groups = sortedItems
            .groupBy { it.second.address.idnd }
            .map {
                listOf(AddressesItem.GroupHeader(it.value.map { it.second }, tasks.size == 1)) +
                        it.value.map { AddressesItem.AddressItem(it.second, it.first) }
            }
            .flatten()

        return groups
    }
}