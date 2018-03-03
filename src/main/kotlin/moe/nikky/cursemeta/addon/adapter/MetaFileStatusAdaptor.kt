package moe.nikky.cursemeta.addon.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.datacontract.schemas._2004._07.curse_addons.FileStatus
import java.io.IOException

object MetaFileStatusAdaptor : TypeAdapter<FileStatus>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: FileStatus?) {
        if (value != null)
            out.value(value.value)
        else
            out.nullValue()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): FileStatus {
        val value = `in`.nextString()
        return FileStatus.Factory.fromValue(value)
    }
}