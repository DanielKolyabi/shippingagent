package ru.relabs.kurjercontroller.presentation.host

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.github.terrakok.cicerone.Command
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.github.terrakok.cicerone.androidx.FragmentScreen
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.utils.extensions.hideKeyboard

class CiceroneNavigator(activity: HostActivity): AppNavigator(activity, R.id.fragment_container) {
    override fun setupFragmentTransaction(
        screen: FragmentScreen,
        fragmentTransaction: FragmentTransaction,
        currentFragment: Fragment?,
        nextFragment: Fragment
    ) {
        super.setupFragmentTransaction(screen, fragmentTransaction, currentFragment, nextFragment)
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
    }

    override fun applyCommands(commands: Array<out Command>) {
        activity.hideKeyboard()
        super.applyCommands(commands)
    }
}