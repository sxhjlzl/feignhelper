package com.lizhuolun.feignhelper.config

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationConfigReaderTest {

    @Test
    fun `recognizes supported Spring configuration files`() {
        assertTrue(ApplicationConfigReader.isSpringConfigFile("application.yml"))
        assertTrue(ApplicationConfigReader.isSpringConfigFile("application-prod.yaml"))
        assertTrue(ApplicationConfigReader.isSpringConfigFile("bootstrap.properties"))
        assertFalse(ApplicationConfigReader.isSpringConfigFile("application.json"))
        assertFalse(ApplicationConfigReader.isSpringConfigFile("other.yml"))
    }
}
