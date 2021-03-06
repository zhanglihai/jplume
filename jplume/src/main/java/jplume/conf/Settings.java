package jplume.conf;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jplume.utils.ExceptionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings {

	public static final String JPLUME_USER_CONFIG_FILE = "USER_CONFIG_FILE";
	
	private static ScriptEngine jsEngine = null;
	
	private static final String defaultConfigFile = "jplume-default.json";
	
	private static Logger logger = LoggerFactory.getLogger(Settings.class);
	
	public static void initalize(String userConfigFile) throws InvalidConfigException {
		ScriptEngineManager sem = new ScriptEngineManager();
		jsEngine = sem.getEngineByName("js");
		readConfig(defaultConfigFile);
		if (userConfigFile != null && !userConfigFile.isEmpty()){
			readConfig(userConfigFile);
		}
	}
	
	private static void readConfig(String configFile) throws InvalidConfigException {
		URL config = null;
		try {
			config = Settings.class.getClassLoader().getResource(configFile);
			if (config == null) {
				throw new InvalidConfigException("Could not read config file '"
					+ configFile + "'");
			}
			jsEngine.eval(new InputStreamReader(config.openStream()));
		} catch (ScriptException e) {
			logger.error("The Config file " + configFile + " has error");
			try {
				ExceptionUtil.logScriptException(logger, e, config);
			} catch (IOException e1) {
				e.printStackTrace();
			}
			throw new InvalidConfigException("The config file '" + configFile + "' has error in line " + e.getLineNumber(), e);
		} catch (IOException e) {
			throw new InvalidConfigException("Could not read config file '"
					+ configFile + "'", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(String key, T defaultVal){
		Object val =  jsEngine.get(key);
		return val == null ? defaultVal : (T)val;
	}
	
	public static boolean getBoolean(String key, boolean defaultVal){
		Object val =  jsEngine.get(key);
		return val == null ? defaultVal : (Boolean)val;
	}
	
	public static String get(String key){
		return get(key, null);
	}
	
	public static boolean isDebug() {
		return true ==  (Boolean)jsEngine.get("DEBUG");
	}
	
	public static boolean isTrue(String key) {
		return true ==  (Boolean)jsEngine.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public static <T1, T2> Map<T1, T2> getMap(String key) {
		return (Map<T1, T2>)jsEngine.get(key);
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> getList(String key) {
		return (List<T>)jsEngine.get(key);
	}

	public static String getDefauleEncoding() {
		return get("DEFAULT_ENCODING", "utf-8");
	}
	
	public static String getDefaultContentType() {
		return get("DEFAULT_CONTENT_TYPE", "text/html");
	}

	public static boolean useEtags() {
		return getBoolean("USE_ETAGS", false);
	}
}
