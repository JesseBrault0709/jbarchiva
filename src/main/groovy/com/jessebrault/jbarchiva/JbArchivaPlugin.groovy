package com.jessebrault.jbarchiva

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

/**
 * This plugin does a few things:
 * <ul>
 *   <li>Configures the dependency repositories for the project to point to both
 *     the internal and snapshots repositories at <a href="https://archiva.jessebrault.com/">
 *     archiva.jessebrault.com</a>.</li>
 *   <li>If the maven-publish plugin is present (after evaluate), configures
 *     it to publish to either the internal or snapshots repository at archiva.jessebrault.com,
 *     depending on if the {@code project.version} property ends with {@code 'SNAPSHOT'}
 *     or not. This uses the provided username and password (see below) to login
 *     to archiva.jessebrault.com.</li>
 * </ul>
 * <p>
 * Properties available via DSL extension:
 * <ul>
 *   <li>{@code String username}: the username to login to jbArchiva (default:
 *      {@code System.getenv('JBARCHIVA_USERNAME') ?: '')}.</li>
 *   <li>{@code String password}: the password to login to jbArchiva (default:
 *      {@code System.getenv('JBARCHIVA_PASSWORD') ?: '')}.</li>
 */
class JbArchivaPlugin implements Plugin<Project> {

    static final INTERNAL_URI = new URI('https://archiva.jessebrault.com/repository/internal')
    static final SNAPSHOTS_URI = new URI('https://archiva.jessebrault.com/repository/snapshots')

    @Override
    void apply(Project project) {
        // Get extension
        def jbArchivaExtension = project.extensions.create('jbArchiva', JbArchivaExtension)

        // Conventions
        jbArchivaExtension.username.convention System.getenv('JBARCHIVA_USERNAME') ?: ''
        jbArchivaExtension.password.convention System.getenv('JBARCHIVA_PASSWORD') ?: ''

        // Configure the repositories for dependencies
        project.repositories.maven { MavenArtifactRepository repository ->
            repository.name = 'jbArchivaInternal'
            repository.url = INTERNAL_URI
            repository.credentials {
                username = jbArchivaExtension.username.get()
                password = jbArchivaExtension.password.get()
            }
        }

        project.repositories.maven { MavenArtifactRepository repository ->
            repository.name = 'jbArchivaSnapshots'
            repository.url = SNAPSHOTS_URI
            repository.credentials {
                username = jbArchivaExtension.username.get()
                password = jbArchivaExtension.password.get()
            }
        }

        // If MavenPublish is present, configure it to point to the repositories
        project.afterEvaluate {
            def mavenPublish = project.plugins.findPlugin(MavenPublishPlugin)
            if (mavenPublish != null) {
                def publishingExtension = project.extensions.getByType(PublishingExtension)
                publishingExtension.repositories.maven { MavenArtifactRepository repository ->
                    def isSnapshot = (project.version as String).endsWith('SNAPSHOT')
                    repository.name = isSnapshot ? 'jbArchivaSnapshots' : 'jbArchivaInternal'
                    repository.url = isSnapshot ? SNAPSHOTS_URI : INTERNAL_URI
                    repository.credentials {
                        username = jbArchivaExtension.username.get()
                        password = jbArchivaExtension.password.get()
                    }
                }
            }
        }
    }

}
