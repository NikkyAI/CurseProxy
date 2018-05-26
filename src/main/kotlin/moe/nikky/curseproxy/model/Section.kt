package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by nikky on 26/05/18.
 * @author Nikky
 * @version 1.0
 */
enum class Section {
    @JsonProperty("modpacks") MODPACKS,
    @JsonProperty("mc-mods") MCMODS,
    @JsonProperty("texture-packs") TEXTUREPACKS,
    @JsonProperty("worlds") WORLDS;

//    companion object {
//        @JsonCreator
//        @JvmStatic
//        fun fromString(key: String?): ProjectStatus? {
//            LOG.info("parsing section " + key)
//            return if (key == null)
//                null
//            else {
//                val index = key.toIntOrNull() ?: return ProjectStatus.valueOf(key.replace("-", "").toUpperCase())
//                return ProjectStatus.values()[index - 1]
//            }
//        }
//    }
}