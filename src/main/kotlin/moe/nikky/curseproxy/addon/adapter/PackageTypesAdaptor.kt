package moe.nikky.curseproxy.addon.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.datacontract.schemas._2004._07.curse_addons.PackageTypes
import java.io.IOException

object PackageTypesAdaptor : TypeAdapter<PackageTypes>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: PackageTypes?) {
        if (value != null)
            out.value(value.value)
        else
            out.nullValue()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): PackageTypes {
        val value = `in`.nextString()
        return PackageTypes.Factory.fromValue(value)
    }
}