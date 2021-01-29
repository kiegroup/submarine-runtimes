/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.compiler.xml.processes;

import org.drools.compiler.compiler.xml.XmlDumper;
import org.drools.core.xml.ExtensibleXmlParser;
import org.jbpm.workflow.core.JbpmNode;
import org.jbpm.workflow.core.node.MilestoneNode;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class MilestoneNodeHandler extends AbstractNodeHandler {

    protected JbpmNode createNode() {
        return new MilestoneNode();
    }

    @SuppressWarnings("unchecked")
	public Class generateNodeFor() {
        return MilestoneNode.class;
    }

    public void handleNode( final JbpmNode node, final Element element, final String uri,
                            final String localName, final ExtensibleXmlParser parser)
            throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        MilestoneNode milestoneNode = (MilestoneNode) node;
        for (String eventType: milestoneNode.getActionTypes()) {
        	handleAction(milestoneNode, element, eventType);
        }
    }
    
    public void writeNode( JbpmNode node, StringBuilder xmlDump, boolean includeMeta) {
		MilestoneNode milestoneNode = (MilestoneNode) node;
		writeNode("milestone", milestoneNode, xmlDump, includeMeta);
        String constraint = milestoneNode.getCondition();
        if (constraint != null || milestoneNode.getTimers() != null || milestoneNode.containsActions()) {
            xmlDump.append(">\n");
            if (includeMeta) {
            	writeMetaData(milestoneNode, xmlDump);
            }
            if (constraint != null) {
            	xmlDump.append("      <constraint type=\"rule\" dialect=\"mvel\" >"
            			+ XmlDumper.replaceIllegalChars(constraint.trim()) + "</constraint>" + EOL);
            }
            for (String eventType: milestoneNode.getActionTypes()) {
            	writeActions(eventType, milestoneNode.getActions(eventType), xmlDump);
            }
            writeTimers(milestoneNode.getTimers(), xmlDump);
            endNode("milestone", xmlDump);
        } else {
            endNode(xmlDump);
        }
	}

}
