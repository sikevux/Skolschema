package tobbe.android.skolschema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Settings {
	
	/* Class from my game: Current for Android */

	private static final String SETTINGS_FILE = "/sdcard/Skolschema/settings";
	private static final String NAME_VALUE_DELIMITER = "|";
	
	// List of available settings
	public static final String CHECK_FOR_UPDATES = "CHECK_FOR_UPDATES";
	public static final String UPDATE_SCHEDULES_ON_MOBILE_NETWORK = "UPDATE_SCHEDULE_ON_MOBILE_NETWORK";
	public static final String FULLSCREEN_MODE = "FULLSCREEN_MODE";
	
	// Default values
	private static final String[] SETTINGS_DEFAULT_VALUES = { CHECK_FOR_UPDATES, "true",
															  UPDATE_SCHEDULES_ON_MOBILE_NETWORK, "true",
															  FULLSCREEN_MODE, "true"};
	
	// Every setting and its value gets stored in this map
	private static Map<String, String> settings = new HashMap<String, String>();
	
	public static boolean loadSettings() {
		if(!new File(SETTINGS_FILE).exists()) {
			createDefaultSettings();
			return false;
		}
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(SETTINGS_FILE))));
			
			String line = null;
			while((line = in.readLine()) != null) {
				try {
					StringTokenizer st = new StringTokenizer(line, NAME_VALUE_DELIMITER);			
					settings.put(st.nextToken(), st.nextToken());
				} catch(NoSuchElementException e) {
					Logger.log("Ignoring unknown line in settings file.");
					continue;
				}
			}
			
			in.close();
		} catch(IOException e) {
			Logger.log("Creating default settings (settings couldn't be loaded).");
			createDefaultSettings();
			return false;
		}
		
		// Create all missing settings
		createMissingSettings();
		
		return true;
	}
	
	public static boolean saveSettings() {
		try {
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(SETTINGS_FILE))));
			
			for(Entry<String, String> e : settings.entrySet()) {
				out.println(e.getKey() + NAME_VALUE_DELIMITER + e.getValue());
			}
			
			out.close();
		} catch(IOException e) {
			Logger.warningMessage("Couldn't save settings (changed settings will be reset when you restart the application).");
			Logger.log("Couldn't save settings.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Creates the default settings.
	 */
	public static void createDefaultSettings() {
		// Clear loaded settings
		settings.clear();
		
		for(int s = 0; s < SETTINGS_DEFAULT_VALUES.length / 2; s++) {
			setString(SETTINGS_DEFAULT_VALUES[s * 2], SETTINGS_DEFAULT_VALUES[s * 2 + 1]);
		}
		
		// Save settings
		saveSettings();
	}
	
	/**
	 * Creates every missing setting, giving them their default values.
	 */
	public static void createMissingSettings() {
		for(int s = 0; s < SETTINGS_DEFAULT_VALUES.length / 2; s++) {
			if(!settings.containsKey(SETTINGS_DEFAULT_VALUES[s * 2])) {
				setString(SETTINGS_DEFAULT_VALUES[s * 2], SETTINGS_DEFAULT_VALUES[s * 2 + 1]);
			}
		}
		
		// Save settings
		saveSettings();
	}
	
	public static String getString(String name) {
		String s = settings.get(name);
		
		if(s == null) {
			Logger.log("Setting with name: '" + name + "' doesn't exist (or is null).");
		}
		
		return s;
	}

	public static int getInteger(String name) {
		String s = settings.get(name);
		
		if(s == null) {
			Logger.log("Setting with name: '" + name + "' doesn't exist (or is null).");
		}
		
		try {
			int i = Integer.parseInt(s);
			return i;
		} catch(NumberFormatException e) {
			Logger.log("Cannot convert setting to integer: '" + name + "'.");
			return 0;
		}
	}

	public static boolean getBoolean(String name) {
		String s = settings.get(name);
		
		if(s == null) {
			Logger.log("Setting with name: '" + name + "' doesn't exist (or is null).");
		}

		return Boolean.parseBoolean(s);
	}
	
	public static void setString(String name, String value) {
		if(settings.containsKey(name)) {
			settings.remove(name);
			settings.put(name, value);
		} else {
			settings.put(name, value);
		}
	}
	
	public static void setInteger(String name, int value) {
		if(settings.containsKey(name)) {
			settings.remove(name);
			settings.put(name, String.valueOf(value));
		} else {
			settings.put(name, String.valueOf(value));
		}
	}

	public static void setBoolean(String name, boolean value) {
		if(settings.containsKey(name)) {
			settings.remove(name);
			settings.put(name, String.valueOf(value));
		} else {
			settings.put(name, String.valueOf(value));
		}
	}
}
