package moe.nikky.curseproxy.model

/**
 * Created by nikky on 26/05/18.
 * @author Nikky
 * @version 1.0
 */
enum class Section(val id: Int, val sectionName: String, val gameId: Int) {
    // World of Warcraft
    ADDONS(1, "addons", 1),
    // The Secret World
    TSW_MODS(14, "tws-mods", 64),
    // Runes of magic
    ROM_ADDONS(4571, "addons", 335),
    // World of Tanks
    WOT_MODS(8, "wot-mods", 423),
    WOT_SKINS(9, "wot-skins", 423),
    // Rift
    RIFT_ADDONS(4564, "addons", 424),
    // Minecraft
    MODS(6, "mc-mods", 432),
    TEXTURE_PACKS(12, "texture-packs", 432),
    WORLDS(17, "worlds", 432),
    MODPACKS(4471, "modpacks", 432),
    // Wildstar
    WS_ADDONS(18, "ws-addons", 454),
    // The Elder Scrolls Online
    TESO_ADDONS(19, "teso-addons", 455),
    // Darkest Dungeon
    DD_MODS(4613, "dd-mods", 608),
    // Stardew Valley
    STARDEW_VALLEY_MODS(4643, "mods", 669)
    ;

    companion object {
        fun fromCategory(categorySection: CategorySection) = Section.values().find {
            it.gameId == categorySection.gameID && it.id == categorySection.id
        }
        fun fromId(id: Int) = Section.values().find {
            it.id == id
        }
    }
}