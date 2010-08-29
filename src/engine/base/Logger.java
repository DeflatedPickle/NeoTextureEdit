/**
    Copyright (C) 2010  Holger Dammertz

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package engine.base;

import engine.parameters.IntParam;
import engine.parameters.LocalParameterManager;

/**
 * This is a simple logging interface I use to output and enable/disable
 * the log messages. 
 * @author Holger Dammertz
 *
 */
public class Logger extends LocalParameterManager {
	private static Logger ms_Logger;
	private static final int DEFAULT_LOG_LEVEL = 3;
	
	// 0: log nothing (except fatal), 3 log all
	private IntParam pLogLevel = CreateLocalIntParam("Log Level", DEFAULT_LOG_LEVEL, 0, 3);
	
	public static final Logger getInstance() {
		if (ms_Logger == null) ms_Logger = new Logger();
		return ms_Logger;
	}
	
	public static final void log(Object source, String message) {
		getInstance()._log(source, message);
	}

	public static final void logWarning(Object source, String message) {
		getInstance()._logWarning(source, message);
	}
	public static final void logError(Object source, String message) {
		getInstance()._logError(source, message);
	}
	public static final void logFatal(Object source, String message) {
		getInstance()._logFatal(source, message);
	}
	
	private Logger() {
	}
	
	
	private void _log(Object source, String message) {
		if (pLogLevel.get() > 2) {
			System.out.println(message);
		}
	}
	
	private void _logWarning(Object source, String message) {
		if (pLogLevel.get() > 1) {
			System.out.println("WARNING: " + message);
		}
	}
	
	private void _logError(Object source, String message) {
		if (pLogLevel.get() > 0) {
			System.err.println("ERROR: " + message);
		}
	}

	
	private void _logFatal(Object source, String message) {
		System.err.println("FATAL: " + message);
		System.exit(0);
	}
}
