package com.lightsocks.socks5.util;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

public class ConfigUtil {

	private HashMap<String, String> values = new HashMap<String, String>();

	public void parseConfig(String[] args) {
		for (String arg : args) {
			String[] pair = arg.split("=");
			if (pair != null && pair.length == 2) {
				values.put(pair[0], pair[1]);
			} else if (pair != null && pair.length == 1) {
				values.put(pair[0], pair[0]);
			}
		}
		parseProperites();
	}

	public String getValue(String key) {
		return values.get(key);
	}

	private void parseProperites() {
		try {
			Properties props = null;
			if (values.containsKey("-c")) {
				props = new Properties();
				try {
					props.load(new FileInputStream(values.get("-c")));
				} catch (Exception ex) {
					props = null;
				}
			} 
			if (props == null) {
				try {
					props = new Properties();
					props.load(new FileInputStream("config.properties"));
				} catch (Exception ex) {
					props = null;
				}
				if (props == null) {
					try {
						props = new Properties();
						props.load(ConfigUtil.class.getClassLoader()
								.getResourceAsStream("config.properties"));
					} catch (Exception ex) {
						props = null;
					}
				}
			}
			if (props != null) {
				String serverIp = props.getProperty("server.ip");
				String serverPort = props.getProperty("server.port");
				String localIp = props.getProperty("local.ip");
				String localPort = props.getProperty("local.port");
				String password = props.getProperty("password");
				String mode = props.getProperty("method");

				if (!values.containsKey("-s")) {
					values.put("-s", serverIp);
				}
				if (!values.containsKey("-p")) {
					values.put("-p", serverPort);
				}
				if (!values.containsKey("-i")) {
					values.put("-i", localIp);
				}
				if (!values.containsKey("-l")) {
					values.put("-l", localPort);
				}
				if (!values.containsKey("-k")) {
					values.put("-k", password);
				}
				if (!values.containsKey("-m")) {
					values.put("-m", mode);
				}
			}

		} catch (Exception ex) {

		}

	}
}
