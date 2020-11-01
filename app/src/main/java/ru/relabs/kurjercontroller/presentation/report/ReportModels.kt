package ru.relabs.kurjercontroller.presentation.report

import android.net.Uri
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.data.database.entities.EntranceResultEntity
import ru.relabs.kurjercontroller.domain.models.ApartmentResult
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.EntrancePhoto
import ru.relabs.kurjercontroller.domain.providers.PathsProvider
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.presentation.base.tea.*
import ru.relabs.kurjercontroller.presentation.reportPager.ReportTaskWithItem
import java.io.File
import java.util.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

data class PhotoWithUri(val photo: EntrancePhoto, val uri: Uri)

data class ReportState(
    val task: ReportTaskWithItem? = null,
    val entrance: Entrance? = null,
    val defaultReportType: ReportApartmentButtonsMode = ReportApartmentButtonsMode.Main,
    val saved: EntranceResultEntity? = null,
    val savedApartments: List<ApartmentResult> = emptyList(),
    val entranceEuroKeys: List<String> = emptyList(),
    val selectedEuroKey: String? = null,
    val entranceKeys: List<String> = emptyList(),
    val selectedKey: String? = null,

    val selectedEntrancePhotos: List<PhotoWithUri> = emptyList(),

    val loaders: Int = 0
)

class ReportContext(val errorContext: ErrorContextImpl = ErrorContextImpl()) :
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl(),
    KoinComponent {

    val databaseRepository: DatabaseRepository by inject()
    val controlRepository: ControlRepository by inject()
    val pathsProvider: PathsProvider by inject()

    var requestPhoto: (entrance: Int, multiplePhoto: Boolean, targetFile: File, uuid: UUID) -> Unit = { _, _, _, _ -> }
}

typealias ReportMessage = ElmMessage<ReportContext, ReportState>
typealias ReportEffect = ElmEffect<ReportContext, ReportState>
typealias ReportRender = ElmRender<ReportState>