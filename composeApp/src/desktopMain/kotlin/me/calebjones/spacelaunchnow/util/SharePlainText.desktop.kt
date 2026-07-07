package me.calebjones.spacelaunchnow.util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual fun sharePlainText(text: String, subject: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}
