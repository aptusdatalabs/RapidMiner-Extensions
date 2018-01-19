/**
 * RapidMiner PMML Extension
 *
 * Copyright (C) 2001-2015 by RapidMiner and the contributors
 *
 * Complete list of developers available at our web site:
 *
 * http://rapidminer.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Properties;

//import com.rapidminer.extension.professional.PluginInitProfessional;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.license.LicenseManagerRegistry;
//import com.rapidminer.license.verification.JarVerifier;


/**
 * This class provides hooks for initialization
 *
 * @author Sebastian Land
 */
public class PluginInitPMML {

	static {
		verifyInstallation();
	}

	public static void initGui(MainFrame mainframe) {}

	public static InputStream getOperatorStream(ClassLoader loader) {
		return null;
	}

	public static void initPluginManager() {}

	public static void initFinalChecks() {}

	public static void initSplashTexts() {}

	public static void initAboutTexts(Properties aboutBoxProperties) {}

	public static Boolean showAboutBox() {
		return true;
	}

	/**
	 * Calls the JarVerifier to ensure that the user has the correct license to use this plug in.
	 */
	public static void verifyInstallation() {
/*		try {
			JarVerifier.verify(LicenseManagerRegistry.INSTANCE.get().getClass(), RapidMiner.class, PluginInitPMML.class,
					PluginInitProfessional.class);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}*/
	}

	/**
	 * This extension should not use the default extension grouping.
	 *
	 * @return {@code false} in all cases
	 */
	public static Boolean useExtensionTreeRoot() {
		return false;
	}
}
