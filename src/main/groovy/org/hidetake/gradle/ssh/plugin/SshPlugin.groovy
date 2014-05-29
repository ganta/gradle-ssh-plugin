package org.hidetake.gradle.ssh.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hidetake.gradle.ssh.internal.SshTaskService

/**
 * Gradle SSH plugin.
 * This class adds project extension and convention properties.
 *
 * @author hidetake.org
 */
class SshPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.ssh = new CompositeSettings()
        project.extensions.remotes = createRemoteContainer(project)

        project.convention.plugins.ssh = new Convention(project)
    }

    private static createRemoteContainer(Project project) {
        def remotes = project.container(Remote)
        remotes.metaClass.mixin(RemoteContainerExtension)
        def parentRemotes = project.parent?.extensions?.findByName('remotes')
        if (parentRemotes instanceof NamedDomainObjectContainer<Remote>) {
            parentRemotes.each { Remote parentRemote ->
                remotes.add(parentRemote.clone() as Remote)
            }
        }
        remotes
    }

    static class Convention {
        private final Project project

        private Convention(Project project1) {
            project = project1
            assert project
        }

        /**
         * Alias to omit import in build script.
         */
        final Class SshTask = org.hidetake.gradle.ssh.plugin.SshTask

        /**
         * Execute a SSH closure.
         *
         * @param closure closure for {@link org.hidetake.gradle.ssh.internal.DefaultSshTaskHandler}
         * @return returned value of the last session
         */
        Object sshexec(Closure closure) {
            assert closure, 'closure must be given'
            SshTaskService.instance.execute(project.extensions.ssh as CompositeSettings, closure)
        }
    }
}
