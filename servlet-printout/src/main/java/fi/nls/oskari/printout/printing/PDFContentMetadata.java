package fi.nls.oskari.printout.printing;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchema;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

/**
 * Define XMP properties used with the Dublin Core specification.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDFContentMetadata extends XMPSchema {
	/**
	 * The namespace for this schema.
	 */
	public static final String NAMESPACE = "http://oskari.org/printout/metadata/0.1/";

	/**
	 * Construct a new blank Dublin Core schema.
	 * 
	 * @param parent
	 *            The parent metadata schema that this will be part of.
	 */
	public PDFContentMetadata(XMPMetadata parent) {
		super(parent, "oskari", NAMESPACE);
	}

	/**
	 * Constructor from existing XML element.
	 * 
	 * @param element
	 *            The existing element.
	 * @param prefix
	 *            The schema prefix.
	 */
	public PDFContentMetadata(Element element, String prefix) {
		super(element, prefix);
	}

	/**
	 * Remove a creator from the list of creators.
	 * 
	 * @param creator
	 *            The author of the resource.
	 */
	public void removeCreator(String creator) {
		removeSequenceValue(prefix + ":creator", creator);
	}

	/**
	 * Add a creator.
	 * 
	 * @param creator
	 *            The author of the resource.
	 */
	public void addCreator(String creator) {
		addSequenceValue(prefix + ":creator", creator);
	}

	/**
	 * Get a complete list of creators.
	 * 
	 * @return A list of java.lang.String objects.
	 */
	public List<String> getCreators() {
		return getSequenceList(prefix + ":creator");
	}

	public void setMetadataFromMap(Map<String, ?> metas) {
		for (String key : metas.keySet()) {
			String val = (String) metas.get(key);
			setTextProperty(prefix + ":" + key, val);
		}

	}

}