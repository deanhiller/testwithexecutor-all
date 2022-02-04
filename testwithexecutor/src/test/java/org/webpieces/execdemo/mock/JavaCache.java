package org.webpieces.execdemo.mock;

import java.io.File;

import org.webpieces.util.file.FileFactory;

public class JavaCache {

	public static File getCacheLocation() {
		return FileFactory.newCacheLocation("testwithexecutorCache/precompressedFiles");
	}
	
}
