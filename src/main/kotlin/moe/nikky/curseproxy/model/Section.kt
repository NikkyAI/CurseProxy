//package moe.nikky.curseproxy.model
//
///**
// * Created by nikky on 26/05/18.
// * @author Nikky
// * @version 1.0
// */
//enum class Section(val id: Int, val gameId: Int) {
//    Mods(id=8, gameId=432),
//    Modpacks(id=11, gameId=432),
//    Addons(id=1, gameId=1),
//    TexturePacks(id=9, gameId=432),
//    Mods(id=19, gameId=61489),
//    Mods(id=17, gameId=4455),
//    Worlds(id=10, gameId=432),
//    Addons(id=13, gameId=454),
//    Mods(id=16, gameId=4401),
//    Addons(id=4, gameId=335),
//    Mods(id=5, gameId=423),
//    Skins(id=6, gameId=423),
//    Addons(id=14, gameId=455),
//    Mods(id=3, gameId=64),
//    Addons(id=7, gameId=424),
//    Mods(id=20, gameId=669),
//    Mods(id=12, gameId=449),
//    Addons(id=22, gameId=70667),
//    Mods(id=21, gameId=18237),
//    Mods(id=15, gameId=608)
//    ;
//
//    companion object {
//        fun fromCategory(categorySection: CategorySection) = Section.values().find {
//            it.gameId == categorySection.gameID && it.id == categorySection.id
//        }
//        fun fromId(id: Int) = Section.values().find {
//            it.id == id
//        }
//    }
//}