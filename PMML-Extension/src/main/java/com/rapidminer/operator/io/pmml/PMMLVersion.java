/**
 * RapidMiner PMML Extension
 *
 * Copyright (C) 2001-2015 by RapidMiner and the contributors
 *
 * Complete list of developers available at our web site:
 *
 *      http://rapidminer.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.io.pmml;

/**
 * An enumeration over all supported PMML versions.
 * 
 * @author Sebastian Land
 */
public enum PMMLVersion {
	VERSION_3_2("3.2"),
	VERSION_4_0("4.0"),
	VERSION_4_3("4.3");
	
	private String versionIdentifier;
	
	private PMMLVersion(String versionString) {
		this.versionIdentifier = versionString;
	}
	
	public String getVersion() {
		return versionIdentifier;
	}
	
	public static String[] getVersionIdentifiers() {
		PMMLVersion[] versions = PMMLVersion.values();
		String[] result = new String[versions.length];
		for (int i = 0; i < versions.length; i++) {
			result[i] = versions[i].getVersion();
		}
		return result;
	}
}
