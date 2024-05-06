package com.jessebrault.jbarchiva

import org.gradle.api.provider.Property

interface JbArchivaExtension {
    Property<String> getUsername()
    Property<String> getPassword()
}
