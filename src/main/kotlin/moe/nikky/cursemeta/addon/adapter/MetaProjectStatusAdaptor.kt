package moe.nikky.cursemeta.addon.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.datacontract.schemas._2004._07.curse_addons.ProjectStage
import org.datacontract.schemas._2004._07.curse_addons.ProjectStatus
import java.io.IOException

object MetaProjectStatusAdaptor : TypeAdapter<ProjectStatus>() {
    override fun write(out: JsonWriter, value: ProjectStatus?) {
        if (value != null)
            out.value(value.value)
        else
            out.nullValue()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): ProjectStatus {
        val value = `in`.nextString()
        return ProjectStatus.Factory.fromValue(value)
    }
}