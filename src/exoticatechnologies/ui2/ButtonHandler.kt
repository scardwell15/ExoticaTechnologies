package exoticatechnologies.ui2

fun interface ClickHandler {
    fun checked()
}

fun interface MouseEnterHandler {
    fun mouseEntered()
}

fun interface MouseExitHandler {
    fun mouseExited()
}

class ComponentHandlers {
    val clicks = mutableListOf<ClickHandler>()
    val mouseEnters = mutableListOf<MouseEnterHandler>()
    val mouseExits = mutableListOf<MouseExitHandler>()

    fun clicked() {
        clicks.forEach { it.checked() }
    }

    fun mouseEntered() {
        mouseEnters.forEach { it.mouseEntered() }
    }

    fun mouseExited() {
        mouseExits.forEach { it.mouseExited() }
    }
}