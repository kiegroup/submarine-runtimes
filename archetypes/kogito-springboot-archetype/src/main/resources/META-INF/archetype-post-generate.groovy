// Performs the post processing tasks in the generated project
import groovy.xml.XmlParser

File pomFile = new File(request.getOutputDirectory() + "/" + request.getArtifactId() + "/pom.xml")
Node pomXml = new XmlParser().parse(pomFile)
