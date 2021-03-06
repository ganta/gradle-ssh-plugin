package org.hidetake.gradle.ssh.internal.interaction

import org.hidetake.gradle.ssh.plugin.interaction.Stream
import org.hidetake.gradle.ssh.plugin.interaction.Wildcard
import spock.lang.Specification

class InteractionDelegateSpec extends Specification {

    def '_ is the wildcard'() {
        expect:
        InteractionDelegate._ instanceof Wildcard
    }

    def 'standard output constant'() {
        expect:
        InteractionDelegate.standardOutput instanceof Stream
    }

    def 'standard error constant'() {
        expect:
        InteractionDelegate.standardError instanceof Stream
    }

    def 'standard input constant'() {
        given:
        def standardInputMock = Mock(OutputStream)
        def interactionDelegate = new InteractionDelegate(standardInputMock)

        expect:
        interactionDelegate.standardInput == standardInputMock
    }

    def 'evaluate() returns an empty list'() {
        given:
        def interactionDelegate = new InteractionDelegate(Mock(OutputStream))

        when:
        def interactionRules = interactionDelegate.evaluate {
        }

        then:
        interactionRules == []
    }

    def 'when() adds an interaction rule'() {
        given:
        def interactionDelegate = new InteractionDelegate(Mock(OutputStream))

        when:
        def interactionRules = interactionDelegate.evaluate {
            when(line: 'value') {}
        }

        then:
        interactionRules.size() == 1
        interactionRules[0].condition == [line: 'value']
    }

    def 'multiple when()'() {
        given:
        def interactionDelegate = new InteractionDelegate(Mock(OutputStream))

        when:
        def interactionRules = interactionDelegate.evaluate {
            when(line: 'value1') {}
            when(nextLine: 'value2') {}
            when(partial: 'value3') {}
        }

        then:
        interactionRules.size() == 3
        interactionRules[0].condition == [line: 'value1']
        interactionRules[1].condition == [nextLine: 'value2']
        interactionRules[2].condition == [partial: 'value3']
    }

    def 'results of evaluate() are dependent'() {
        given:
        def interactionDelegate = new InteractionDelegate(Mock(OutputStream))

        when:
        def interactionRules1 = interactionDelegate.evaluate {
            when(line: 'value1') {}
            when(nextLine: 'value2') {}
        }
        def interactionRules2 = interactionDelegate.evaluate {
            when(partial: 'value3') {}
        }

        then:
        interactionRules1.size() == 2
        interactionRules1[0].condition == [line: 'value1']
        interactionRules1[1].condition == [nextLine: 'value2']

        interactionRules2.size() == 1
        interactionRules2[0].condition == [partial: 'value3']
    }

}
