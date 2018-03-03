package moe.nikky.cursemeta.addon.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.datacontract.schemas._2004._07.curse_addons.ProjectStage
import java.io.IOException

object MetaProjectStageAdaptor : TypeAdapter<ProjectStage>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: ProjectStage?) {
        if (value != null)
            out.value(value.value)
        else
            out.nullValue()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): ProjectStage {
        val value = `in`.nextString()
        return ProjectStage.Factory.fromValue(value)
    }
}