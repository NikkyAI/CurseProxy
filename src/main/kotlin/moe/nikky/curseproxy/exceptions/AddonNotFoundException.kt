package moe.nikky.curseproxy.exceptions

import voodoo.data.curse.ProjectID

/**
 * Created by nikky on 01/05/18.
 * @author Nikky
 * @version 1.0
 */
open class AddonNotFoundException(
        val unknown_id: Int
) : MessageException("CurseAddon not found")