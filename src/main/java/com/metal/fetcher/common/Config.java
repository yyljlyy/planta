package com.metal.fetcher.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
	
	private static Logger log = LoggerFactory.getLogger(Config.class);

	// TODO
	static {
		init();
	}

	public static final Integer HTTP_MAX_TOTAL = getIntProperty("http.max.total");
	public static final Integer HTTP_MAX_ROUTE = getIntProperty("http.max.route");
	public static final Integer HTTP_CONN_TIMEOUT = getIntProperty("http.conn.timeout");
	public static final Integer HTTP_SOCKET_TIMEOUT = getIntProperty("http.socket.timeout");
	
	private static Properties props;

	private Config() {
		// should not call
	}

	private static void init() {
		props = loadConfig();
	}

	private static Properties loadConfig() {
		final Properties p = new Properties();
		loadDefaultConfig(p);
		return p;
	}
	private static void loadDefaultConfig(final Properties props) {
//		InputStream input = null;
		FileInputStream input = null;
		try {
//			input = Config.class.getClassLoader().getResourceAsStream("load.properties");
			input = new FileInputStream("C:/PhilWork/phil workspace/planta/src/main/resources/load.properties");
		} catch (Exception e) {
			log.error("config file read failed. ", e);
			System.exit(1);
		}
		if (input == null) {
			log.error("default Config File not found: load.properties");
			System.exit(1);
		}
		try {
			props.load(input);
		} catch (final IOException e) {
			e.printStackTrace();
			log.error("load default Config File error: "
					+ e.getMessage());
			System.exit(1);
		} finally {
			try {
				input.close();
			} catch (final IOException e) {
				log.error("input close error:", e);
			}
		}
	}

	public static String getProperty(final String key, final String defaultValue) {
		if (props == null)
			init();
		final String value = props.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return value.trim();
	}

	public static String getProperty(final String key) {
		return getProperty(key, "");
	}

	public static int getIntProperty(String key) {
		final String value = getProperty(key, "0");
		if (value.length() <= 0) {
			return 0;
		}
		return Integer.parseInt(value);
	}
	
	
    public static boolean getBooleanProperty(final String key) {
        final String value = getProperty(key, "false");
        if (value.length() <= 0) {
            return false;
        }
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on");
    }
}
