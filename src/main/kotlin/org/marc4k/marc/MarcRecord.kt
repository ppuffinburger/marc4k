package org.marc4k.marc

class MarcRecord : Record() {
    override val leader = MarcLeader()
}