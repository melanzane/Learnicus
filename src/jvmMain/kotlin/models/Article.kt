package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import org.bson.types.ObjectId

@Serializable
data class ArticleJson(
    @Contextual val _id: ObjectId = ObjectId.get(),
    val sourceId: String,
    val jsonData: String
)