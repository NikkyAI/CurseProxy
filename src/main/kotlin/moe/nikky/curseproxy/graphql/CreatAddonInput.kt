package moe.nikky.curseproxy.graphql

import moe.nikky.curseproxy.model.SparseAddon
import java.time.LocalDate

/**
 * Created by nikky on 25/05/18.
 * @author Nikky
 * @version 1.0
 */
fun CreateAddonInput.toAddon() : SparseAddon {
    return SparseAddon(
            addonId = -1,
            name = this.name,
            primaryAuthorName = this.primaryAuthorName,
            primaryCategoryName = this.primaryCategoryName,
            sectionName = this.sectionName,
            dateModified = this.dateModified,
            dateCreated = this.dateCreated,
            dateReleased = this.dateReleased
    )
}

data class CreateAddonInput(
        val name: String = "",
        val primaryAuthorName: String? = "", //maybe not null ?
        val primaryCategoryName: String? = "",
        val sectionName: String = "",
        val dateModified: LocalDate = LocalDate.now(),
        val dateCreated: LocalDate = LocalDate.now(),
        val dateReleased: LocalDate = LocalDate.now()
)