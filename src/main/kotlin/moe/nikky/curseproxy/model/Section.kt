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
}