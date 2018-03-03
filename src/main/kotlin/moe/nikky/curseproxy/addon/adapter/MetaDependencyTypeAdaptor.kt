package moe.nikky.curseproxy.addon.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.datacontract.schemas._2004._07.curse_addons.DependencyType
import java.io.IOException

object MetaDependencyTypeAdaptor : TypeAdapter<DependencyType>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: DependencyType?) {
        if (value != null)
            out.value(value.value)
        else
            out.nullValue()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): DependencyType {
        val value = `in`.nextString()
        return DependencyType.Factory.fromValue(value)
    }
}