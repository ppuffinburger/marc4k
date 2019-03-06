package org.marc4k

class MarcException : RuntimeException {
    constructor(message: String?, cause: Exception?): super(message, cause)
    constructor(message: String?): super(message)
    constructor(cause: Exception): super(cause)
}