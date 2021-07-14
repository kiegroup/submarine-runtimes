import groovy.xml.XmlParser
import spock.lang.Specification

class AddDependencyToPomTest extends Specification {
    def "Original pom.xml has 5 dependencies"() {
        given:
        Node pomXml = new XmlParser().parse(this.getClass().getResourceAsStream("examplePom.xml"))

        expect:
        pomXml.depthFirst().dependencies.dependency.size() == 5
    }

    def "Add a new dependency to original pom.xml"() {
        given:
        Node pomXml = new XmlParser().parse(this.getClass().getResourceAsStream("examplePom.xml"))

        when:
        Node newDep = new Node(null, "dependency",
                [groupId: "org.kie.kogito", artifactId: "kogito-addons-springboot-cloudevents", version: "2.0.0-SNAPSHOT"])
        pomXml.dependencies[0].children().add(0, newDep)

        then:
        pomXml.depthFirst().dependencies.dependency.size() == 6
    }
}