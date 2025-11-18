package com.sga.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private Jwt jwt = new Jwt();
	private File file = new File();

	@Data
	public static class Jwt {
		private String secret;
		private long expiration;
	}

	@Data
	public static class File {
		private String uploadDir;
		private String spcInputDir;
		private String totvsOutputDir;
	}

	public Object getJwt() {
		// TODO Auto-generated method stub
		return null;
	}
}
