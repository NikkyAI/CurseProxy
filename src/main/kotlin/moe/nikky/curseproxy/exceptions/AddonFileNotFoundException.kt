package moe.nikky.curseproxy.exceptions

import voodoo.data.curse.FileID
import voodoo.data.curse.ProjectID

/**
 * Created by nikky on 01/05/18.
 * @author Nikky
 * @version 1.0
 */
open class AddonFileNotFoundException(
        val unknown_id: ProjectID,
        val unknown_file_id: FileID
) : MessageException("AddonFile not found")