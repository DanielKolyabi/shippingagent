package ru.relabs.kurjercontroller.presentation.host

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import ru.relabs.kurjercontroller.R
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.android.support.SupportAppScreen
import ru.terrakok.cicerone.commands.Command

class CiceroneNavigator(activity: HostActivity): SupportAppNavigator(activity, activity.supportFragmentManager, R.id.fragment_container) {
    override fun createFragment(screen: SupportAppScreen): Fragment? {
        return super.createFragment(screen).also { (activity as? HostActivity)?.onFragmentChanged(it) }
    }

    override fun setupFragmentTransaction(
        command: Command,
        currentFragment: Fragment?,
        nextFragment: Fragment?,
        fragmentTransaction: FragmentTransaction
    ) {
        super.setupFragmentTransaction(
            command,
            currentFragment,
            nextFragment,
            fragmentTransaction
        )
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
    }
}