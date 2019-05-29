package ru.relabs.kurjercontroller.ui.fragments.yandexMap

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.image.ImageProvider
import kotlinx.android.synthetic.main.fragment_yandex_map.*
import kotlinx.coroutines.launch
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.AddressModel

class YandexMapFragment : Fragment() {
    private lateinit var userLocationLayer: UserLocationLayer
    var addressIds: List<AddressIdWithColor> = listOf()
    var addresses: List<AddressWithColor> = listOf()
    val presenter = YandexMapPresenter(this)
    private var callback: Callback? = null

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            addressIds = it.getParcelableArrayList<AddressIdWithColor>("address_ids")?.toList() ?: listOf()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_yandex_map, container, false)
    }

    fun showAddresses() {
        addresses.forEach(::showAddress)
    }

    private fun showAddress(addressWithColor: AddressWithColor) {
        val address = addressWithColor.address
        if (address.lat != 0.0 && address.long != 0.0) {
            val ctx = context ?: return
            val point = Point(address.lat, address.long)

            val color = addressWithColor.color

            mapview.map.mapObjects
                .addPlacemark(Point(address.lat, address.long), ColoredIconProvider(ctx, color))
                .addTapListener { _, _ ->
                    presenter.bgScope.launch {
                        callback?.onAddressClicked(address)
                    }
                    application().router.exit()
                    return@addTapListener true
                }

            mapview.map.mapObjects.addCircle(
                Circle(point, 50f),
                color,
                2f,
                ColorUtils.setAlphaComponent(color, 80)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MapKitFactory.initialize(this.context)

        presenter.loadAddresses()

        val point = Point(application().currentLocation.lat, application().currentLocation.long)
        mapview.map.isRotateGesturesEnabled = false

        mapview.map.move(
            CameraPosition(point, 14f, 0f, 0f)
        )
        userLocationLayer = mapview.map.userLocationLayer
        userLocationLayer.isEnabled = true

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
        presenter.bgScope.cancel()
        MapKitFactory.getInstance().onStop()
        mapview.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapview.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.bgScope.terminate()
    }

    private fun getCameraPosition(addresses: List<AddressModel>): CameraPosition {
        when {
            addresses.isEmpty() -> {
                return CameraPosition(
                    Point(application().currentLocation.lat, application().currentLocation.long),
                    14f, 0f, 0f
                )
            }
            addresses.size == 1 -> {
                val address = addresses.first()
                return CameraPosition(
                    Point(address.lat, address.long),
                    14f, 0f, 0f
                )
            }
            else -> {
                val filtered = addresses.filter { it.lat != 0.0 && it.long != 0.0 }
                val minLat = filtered.minBy { it.lat }?.lat
                val maxLat = filtered.maxBy { it.lat }?.lat
                val minLong = filtered.minBy { it.long }?.long
                val maxLong = filtered.maxBy { it.long }?.long
                if (minLat == null || maxLat == null || minLong == null || maxLong == null) {
                    return getCameraPosition(listOfNotNull(addresses.firstOrNull()))

                }
                return mapview?.map?.cameraPosition(BoundingBox(Point(minLat, minLong), Point(maxLat, maxLong)))
                    ?: getCameraPosition(listOfNotNull(addresses.firstOrNull()))
            }
        }
    }

    fun makeFocus(addresses: List<AddressModel>) {

        mapview?.map?.move(getCameraPosition(addresses))
    }

    companion object {

        @JvmStatic
        fun newInstance(addresses: List<AddressWithColor>) =
            YandexMapFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(
                        "address_ids",
                        ArrayList(addresses.map { AddressIdWithColor(it.address.id, it.color) })
                    )
                }
            }
    }

    data class AddressIdWithColor(
        val id: Int,
        val color: Int = Color.BLUE
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeInt(color)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<AddressIdWithColor> {
            override fun createFromParcel(parcel: Parcel): AddressIdWithColor {
                return AddressIdWithColor(parcel)
            }

            override fun newArray(size: Int): Array<AddressIdWithColor?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class AddressWithColor(
        val address: AddressModel,
        val color: Int = Color.BLUE
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readParcelable(AddressModel::class.java.classLoader),
            parcel.readInt()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(address, flags)
            parcel.writeInt(color)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<AddressWithColor> {
            override fun createFromParcel(parcel: Parcel): AddressWithColor {
                return AddressWithColor(parcel)
            }

            override fun newArray(size: Int): Array<AddressWithColor?> {
                return arrayOfNulls(size)
            }
        }
    }

    interface Callback {
        suspend fun onAddressClicked(address: AddressModel)
    }
}

class ColoredIconProvider(val context: Context, val color: Int) : ImageProvider() {
    override fun getId(): String {
        return "colored:${color}"
    }

    override fun getImage(): Bitmap {
        val drawable = context.resources.getDrawable(R.drawable.house_map_icon)
        val filter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        drawable.colorFilter = filter
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }
}
