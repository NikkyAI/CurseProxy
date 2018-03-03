package moe.nikky

import addons.curse.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.thiakil.curseapi.json.ProjectFeed
import com.thiakil.curseapi.json.adaptors.*
import moe.nikky.cursemeta.addon.adapter.*
import org.datacontract.schemas._2004._07.curse_addons.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

/**
 * Created by nikky on 27/02/18.
 * @author Nikky
 * @version 1.0
 */

fun GsonBuilder.setup(): GsonBuilder = this
        .enableComplexMapKeySerialization()
        .excludeFieldsWithoutExposeAnnotation()
        //.serializeNulls() // do we want to keep nulls for keeping objjects complete
        // or leave them out and let cliuents guess a little ?

        .registerTypeAdapter(PackageTypes::class.java, MetaPackageTypesAdaptor)
        .registerTypeAdapter(DependencyType::class.java, MetaDependencyTypeAdaptor)
        .registerTypeAdapter(ProjectStage::class.java, MetaProjectStageAdaptor)
        .registerTypeAdapter(ProjectStatus::class.java, MetaProjectStatusAdaptor)
        .registerTypeAdapter(FileStatus::class.java, MetaFileStatusAdaptor)
        .registerTypeAdapter(FileType::class.java, MetaFileTypeAdaptor)
        .registerTypeAdapterFactory(AdaptorFactory(::ProjectFeedAdaptor, ProjectFeed::class.java))
        .registerTypeAdapterFactory(AdaptorFactory(::AddOnAdaptor, AddOn::class.java))
        .registerTypeAdapterFactory(AdaptorFactory(::AddOnFileAdaptor, AddOnFile::class.java))
        .registerTypeAdapterFactory(AdaptorFactory(::AddOnFileDependencyAdaptor, AddOnFileDependency::class.java))
        .registerTypeAdapterFactory(AdaptorFactory(::CategorySectionAdaptor, CategorySection::class.java))
        .registerTypeAdapterFactory(AdaptorFactory(::GameVersionLatestFileAdaptor, GameVersionLatestFile::class.java))
        .registerTypeAdapter(AddOnAuthor::class.java, AddOnAuthorAdaptor.INSTANCE)
        .registerTypeAdapter(AddOnAttachment::class.java, AddOnAttachmentAdaptor.INSTANCE)
        .registerTypeAdapter(AddOnCategory::class.java, AddOnCategoryAdaptor.INSTANCE)
        .registerTypeAdapter(AddOnModule::class.java, AddOnModuleAdaptor.INSTANCE)


private val gson = GsonBuilder()
        .setup()
        .setPrettyPrinting()
        .create()

private val flatGson = GsonBuilder()
        .setup()
        .create()


val Any.json: String
    get() = gson.toJson(this)

val Any.flatjson: String
    get() = flatGson.toJson(this)

val Throwable.stackTraceString: String
    get() {
        val sw = StringWriter()
        this.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }