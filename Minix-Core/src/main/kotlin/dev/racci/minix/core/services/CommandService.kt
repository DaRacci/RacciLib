package dev.racci.minix.core.services

import dev.racci.minix.api.extension.Extension
import dev.racci.minix.api.plugin.Minix

class CommandService(override val plugin: Minix) : Extension<Minix>()
