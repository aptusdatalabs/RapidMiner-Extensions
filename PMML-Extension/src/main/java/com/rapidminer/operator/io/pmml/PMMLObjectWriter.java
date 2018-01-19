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

import java.util.Collection;

import org.w3c.dom.Document;

import com.rapidminer.operator.UserError;

/**
 * All classes implementing this interface should fulfill the following contract:
 * They are constructed with the object to export as parameter. They have to be able to 
 * return a XML element reflecting the object. Therefore they get the document for creating new
 * elements.
 * Normally they are not needed anymore and discarded, but should be able to export the
 * model again.
 * 
 * @author Sebastian Land
 */
public interface PMMLObjectWriter {
	
	public abstract Document export(PMMLVersion version) throws UserError;
	
	/**
	 * This method should check the compatibility of the given object and return a detailed list of 
	 * messages explaining, what's not compatible with PMML export.
	 * 
	 * TODO: Why doesn't this method receive an argument? It always returns null.
	 */
	public Collection<String> checkCompatibility();
} 
