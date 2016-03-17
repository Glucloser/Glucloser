package com.nlefler.glucloser.a.models

/**
 * Created by nathan on 2/14/16.
 */
interface Syncable {
    companion object {
        val ModelName: String = "__invalid_model_name__"
        val PrimaryKeyName: String = "__invalid_primary_key_name__"
    }
}
