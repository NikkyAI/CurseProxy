package moe.nikky.cursemeta.addon.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.datacontract.schemas._2004._07.curse_addons.FileStatus
import org.datacontract.schemas._2004._07.curse_addons.FileType
import java.io.IOException

object MetaFileTypeAdaptor : TypeAdapter<FileType>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: FileType?) {
        if (value != null)
            out.value(value.value)
        else
            out.nullValue()
    }

    override fun read(`in`: JsonReader): FileType {
        val value = `in`.nextString()
        return FileType.Factory.fromValue(value)
    }
}