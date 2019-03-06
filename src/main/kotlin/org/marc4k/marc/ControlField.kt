package org.marc4k.marc

data class ControlField(val tag: String, val data: String) {
    override fun toString(): String = "$tag    $data"
}