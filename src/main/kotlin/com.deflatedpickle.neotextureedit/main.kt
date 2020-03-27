package com.deflatedpickle.neotextureedit

import com.bulenkov.darcula.DarculaLaf
import com.mystictri.neotextureedit.TextureEditor
import javax.swing.JPopupMenu
import javax.swing.UIManager

fun main(args: Array<String>) {
    // Needed for the OpenGL canvas
    JPopupMenu.setDefaultLightWeightPopupEnabled(false)
    System.setProperty(
            "org.lwjgl.librarypath",
            System.getProperty("user.dir") + "/build/natives"
    )
    // TODO: Add a theme GUI
    UIManager.setLookAndFeel(DarculaLaf())

    with(TextureEditor(args)) {
        this.createMainFrame()
        this.initialize()
        this.registerForMacOSXEvents()
        this.m_MainFrame.isVisible = true
    }
}