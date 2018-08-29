package moe.nikky.curseproxy.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by nikky on 26/05/18.
 * @author Nikky
 * @version 1.0
 */
enum class Section {
    @JsonProperty("addons") ADDONS,
    @JsonProperty("mc-mods") MC_ADDONS,
    @JsonProperty("texture-packs") TEXTURE_PACKS,
    @JsonProperty("worlds") WORLDS,
    @JsonProperty("modpacks") MODPACKS,
    @JsonProperty("ws-addons") WS_ADDONS,
    @JsonProperty("ksp-mods") KSP_MODS,
    @JsonProperty("tsw-mods") TSW_MODS,
    @JsonProperty("wot-skins") WOT_SKINS,
    @JsonProperty("wot-mods") WOT_MODS,
    @JsonProperty("teso-addons") TESO_ADDONS,
    @JsonProperty("mods") MODS,
    @JsonProperty("tswl-mods") TSWL_MODS,
    @JsonProperty("missions") MISSIONS,
    @JsonProperty("dd-mods") DD_MODS
}