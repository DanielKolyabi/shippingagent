package ru.relabs.kurjercontroller.ui.fragments.yandexMap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.user_location.UserLocationLayer
import kotlinx.android.synthetic.main.fragment_yandex_map.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.AddressModel


class YandexMapFragment : Fragment() {
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var address: AddressModel
    val presenter = YandexMapPresenter(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            address = it.getParcelable("address")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_yandex_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MapKitFactory.initialize(this.context)
        var point = Point(application().currentLocation.lat, application().currentLocation.long)
        mapview.map.isRotateGesturesEnabled = false
        if (address.lat != 0.0 && address.long != 0.0) {
            point = Point(address.lat, address.long)

            mapview.map.mapObjects.addPlacemark(Point(address.lat, address.long))
            mapview.map.mapObjects.addCircle(
                Circle(point, 100f),
                R.color.colorPrimary,
                2f,
                ColorUtils.setAlphaComponent(resources.getColor(R.color.colorAccent), 125)
            )
        }
        mapview.map.move(
            CameraPosition(point, 14f, 0f, 0f)
        )
        userLocationLayer = mapview.map.userLocationLayer
        userLocationLayer.isEnabled = true
        userLocationLayer.isHeadingEnabled = true

        my_position.setOnClickListener {
            mapview.map.move(
                CameraPosition(
                    Point(application().currentLocation.lat, application().currentLocation.long),
                    14f, 0f, 0f
                )
            )
        }
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        mapview.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapview.onStart()
    }


    companion object {

        @JvmStatic
        fun newInstance(address: AddressModel) =
            YandexMapFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("address", address)
                }
            }
    }
}
