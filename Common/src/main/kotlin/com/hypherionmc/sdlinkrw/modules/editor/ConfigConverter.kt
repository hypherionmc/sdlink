package com.hypherionmc.sdlinkrw.modules.editor

import com.google.gson.*
import com.hypherionmc.craterlib.libs.moonconfig.core.CommentedConfig
import com.hypherionmc.craterlib.libs.moonconfig.core.Config
import com.hypherionmc.craterlib.libs.moonconfig.core.UnmodifiableCommentedConfig
import com.hypherionmc.craterlib.libs.moonconfig.core.io.ParsingMode
import com.hypherionmc.craterlib.libs.moonconfig.json.FancyJsonWriter
import com.hypherionmc.craterlib.libs.moonconfig.toml.TomlParser
import com.hypherionmc.craterlib.libs.moonconfig.toml.TomlWriter
import java.util.function.Consumer

object ConfigConverter {

    val gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
    val tomlParser: TomlParser = TomlParser()
    val tomlWriter: TomlWriter = TomlWriter()
    val jsonWriter: FancyJsonWriter = FancyJsonWriter()

    init {
        Config.setInsertionOrderPreserved(true)
    }

    fun readToJson(tomlContent: String): JsonObject {
        val config = CommentedConfig.inMemory()

        tomlParser.parse(tomlContent, config, ParsingMode.ADD)
        val jsonString = jsonWriter.writeToString(config)
        val comments: MutableMap<String, String> = flattenComments(config.comments)

        val finalObject = JsonObject()
        finalObject.add("config", gson.fromJson(jsonString, JsonObject::class.java))
        finalObject.add("comments", gson.toJsonTree(comments))

        return finalObject
    }

    private fun flattenComments(children: Map<String, UnmodifiableCommentedConfig.CommentNode>, prefix: String = "", result: MutableMap<String, String> = mutableMapOf()): MutableMap<String, String> {
        children.forEach { (key, node) ->
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"

            if (!node.comment.isNullOrBlank()) {
                result[fullKey] = node.comment
            }

            node.children?.takeIf { it.isNotEmpty() }?.let {
                flattenComments(it, fullKey, result)
            }
        }

        return result
    }

    fun writeToToml(configJson: JsonObject): String {
        val config = CommentedConfig.inMemory()
        jsonToToml(config, configJson.getAsJsonObject("config"), "")

        val comments = configJson.getAsJsonObject("comments")
        comments.entrySet().forEach { e ->
            try {
                config.setComment(e.key, e.value.asString)
            } catch (_: Exception) {}
        }

        return tomlWriter.writeToString(config)
    }

    private fun jsonToToml(newConfig: CommentedConfig, values: JsonObject, subKey: String) {
        values.entrySet().forEach(Consumer { entry: MutableMap.MutableEntry<String, JsonElement> ->
            val key = entry.key
            val finalKey = if (subKey.isEmpty()) key else "$subKey.$key"

            if (entry.value.isJsonObject) {
                val subConfig = CommentedConfig.inMemory()
                newConfig.add(key, subConfig)
                jsonToToml(subConfig, values.getAsJsonObject(key), finalKey)
            } else {
                if (entry.value.isJsonPrimitive) {
                    val primitive: JsonPrimitive = entry.value.getAsJsonPrimitive()

                    if (primitive.isBoolean) {
                        newConfig.add(key, primitive.getAsBoolean())
                    } else if (primitive.isNumber) {
                        newConfig.add(key, primitive.getAsNumber())
                    } else if (primitive.isString) {
                        newConfig.add(key, primitive.getAsString())
                    } else {
                        throw RuntimeException("$key is not a valid type")
                    }
                } else if (entry.value.isJsonArray) {
                    val array: JsonArray = entry.value.getAsJsonArray()

                    if (!array.isEmpty && array.get(0).isJsonPrimitive && array.get(0).getAsJsonPrimitive().isString) {
                        val collection: MutableList<String?> = ArrayList()

                        array.forEach({ item ->
                            if (item.isJsonPrimitive && item.getAsJsonPrimitive().isString) {
                                collection.add(item.asString)
                            } else {
                                throw RuntimeException("$item is not a valid type")
                            }
                        })
                        newConfig.add(key, collection)
                    } else {
                        val collection: MutableList<Any?> = ArrayList()

                        array.forEach { item ->
                            if (item.isJsonObject) {
                                val subConfig = CommentedConfig.inMemory()
                                jsonToToml(subConfig, item.getAsJsonObject(), finalKey)
                                collection.add(subConfig)
                            } else if (item.isJsonPrimitive) {
                                val primitive: JsonPrimitive = item.getAsJsonPrimitive()

                                if (primitive.isBoolean) {
                                    collection.add(primitive.getAsBoolean())
                                } else if (primitive.isNumber) {
                                    collection.add(primitive.getAsNumber())
                                } else if (primitive.isString) {
                                    collection.add(primitive.getAsString())
                                } else {
                                    throw RuntimeException("$item is not a valid type")
                                }
                            } else {
                                throw RuntimeException("$item is not a valid type")
                            }
                        }
                        newConfig.add(key, collection)
                    }
                } else {
                    throw RuntimeException("$key is not a valid type")
                }
            }
        })
    }

}