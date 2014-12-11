package org.jvnet.jaxb2.maven2.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;
import org.jvnet.jaxb2.maven2.util.CollectionUtils.Function;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.xml.sax.InputSource;

public class IOUtils {

	/**
	 * Creates an input source for the given file.
	 * 
	 * @param file
	 *            file to create input source for.
	 * 
	 * @return Created input source object.
	 */
	public static InputSource getInputSource(File file) {
		try {
			final URL url = file.toURI().toURL();
			return getInputSource(url);
		} catch (MalformedURLException e) {
			return new InputSource(file.getPath());
		}
	}

	public static InputSource getInputSource(final URL url) {
		return new InputSource(StringUtils.escapeSpace(url.toExternalForm()));
	}

	public static InputSource getInputSource(final URI uri) {
		return new InputSource(StringUtils.escapeSpace(uri.toString()));
	}

	public static final Function<File, URL> GET_URL = new Function<File, URL>() {
		public URL eval(File file) {
			try {
				return file.toURI().toURL();
			} catch (MalformedURLException muex) {
				throw new RuntimeException(muex);
			}
		}
	};

	public static final Function<File, Long> LAST_MODIFIED = new Function<File, Long>() {
		public Long eval(File file) {
			return lastModified(file);
		}
	};

	public static long lastModified(File file) {
		if (file == null || !file.exists()) {
			return 0;
		} else {
			return file.lastModified();
		}
	}

	/**
	 * Scans given directory for files satisfying given inclusion/exclusion
	 * patterns.
	 * 
	 * @param directory
	 *            Directory to scan.
	 * @param includes
	 *            inclusion pattern.
	 * @param excludes
	 *            exclusion pattern.
	 * @param defaultExcludes
	 *            default exclusion flag.
	 * @return Files from the given directory which satisfy given patterns. The
	 *         files are {@link File#getCanonicalFile() canonical}.
	 */
	public static List<File> scanDirectoryForFiles(BuildContext buildContext,
			final File directory, final String[] includes,
			final String[] excludes, boolean defaultExcludes)
			throws IOException {
		if (!directory.exists()) {
			return Collections.emptyList();
		}
		final Scanner scanner;

		if (buildContext != null) {
			scanner = buildContext.newScanner(directory, true);
		} else {
			final DirectoryScanner directoryScanner = new DirectoryScanner();
			directoryScanner.setBasedir(directory.getAbsoluteFile());
			scanner = directoryScanner;
		}
		scanner.setIncludes(includes);
		scanner.setExcludes(excludes);
		if (defaultExcludes) {
			scanner.addDefaultExcludes();
		}

		scanner.scan();

		final List<File> files = new ArrayList<File>();
		for (final String name : scanner.getIncludedFiles()) {
			files.add(new File(directory, name).getCanonicalFile());
		}

		return files;
	}

	public static final String JAR_SCHEME = "jar";
	public static final String SEPARATOR = "!/";

	public static URI getMainURIFromJarURI(URI uri)
			throws MalformedURLException, URISyntaxException {
		final URL url = uri.toURL();
		final String spec = url.getFile();
		final int separatorPosition = spec.indexOf(SEPARATOR);
		if (separatorPosition == -1) {
			throw new MalformedURLException("no !/ found in url spec:" + spec);
		}
		final String mainURIString = separatorPosition < 0 ? spec : spec
				.substring(0, separatorPosition);
		return new URI(mainURIString);
	}

	public static Long getLastModifiedForFileURI(URI uri) {
		try {
			final File file = new File(uri);
			if (file.exists()) {
				long lastModified = file.lastModified();
				if (lastModified == 0) {
					// File does not exist or IO exception occured - return null
					// for unknown
					return null;
				} else {
					return lastModified;
				}
			} else {
				// File does not exist - return null for unknown
				return null;
			}
		} catch (IllegalArgumentException iaex) {
			// Error creating a file from URI - return null for unknown
			return null;
		}
	}
}
