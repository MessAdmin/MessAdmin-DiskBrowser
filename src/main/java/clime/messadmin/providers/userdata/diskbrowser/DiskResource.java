/**
 *
 */
package clime.messadmin.providers.userdata.diskbrowser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletContext;

import clime.messadmin.providers.userdata.resourcebrowser.BaseResource;
import clime.messadmin.utils.compress.gzip.GZipUtils;
import clime.messadmin.utils.compress.zip.ZipUtils;

/**
 * Browse the FileSystem
 * @author C&eacute;drik LIME
 */
class DiskResource extends BaseResource {
	private static final int GZIP_MIN_FILE_SIZE = 512; // minimum disk block size

	protected File file;

	public DiskResource(String path) {
		this(path, null);
	}

	protected DiskResource(String path, DiskResource parent) {
		super(path, parent);
		this.file = new File(resourcePath);
	}

	/** {@inheritDoc} */
	public String getPath() {
		String path = super.getPath();
		/* Directories must end with "/" */
		if (file.isDirectory() && ! path.endsWith(File.separator)) {
			path += File.separatorChar;
		}
		return path;
	}

	/** {@inheritDoc} */
	public BaseResource getCanonicalResource() {
		try {
			return new DiskResource(file.getCanonicalPath());
		} catch (IOException ioe) {
			return new DiskResource(file.getAbsolutePath());
		}
	}

	/** {@inheritDoc} */
	public Collection/*<DiskResource>*/ getChildResources(ServletContext context) {
		File[] children = file.listFiles();
		if (children == null) {
			// should never happen: "resource" is not a directory!
			return Collections.EMPTY_LIST;
		}
		Collection result = new ArrayList(children.length);
		for (int i = 0; i < children.length; ++i) {
			File child = children[i];
			result.add(new DiskResource(child.getPath(), this));
		}
		return result;
	}

	/** {@inheritDoc} */
	protected BaseResource getParentDirectoryInternal() {
		String parent = file.getParent();
		if (parent == null) {
			return RootDiskResource.INSTANCE;
		} else {
			return new DiskResource(parent);
		}
	}

	/** {@inheritDoc} */
	public String getFileName() {
		return file.getName();
	}

	/** {@inheritDoc} */
	public boolean isFile() {
		return ! resourcePath.equals("..") && ! resourcePath.equals(".") && file.isFile();
	}
	/** {@inheritDoc} */
	public boolean isDirectory() {
		return resourcePath.equals("..") || resourcePath.equals(".") || file.isDirectory();
	}

	/** {@inheritDoc} */
	public boolean isHidden() {
		return file.isHidden();
	}

	/** {@inheritDoc} */
	public boolean canRead() {
		return file.canRead();
	}
	/** {@inheritDoc} */
	public boolean canWrite() {
		return file.canWrite();
	}

	/** {@inheritDoc} */
	public boolean canDelete() {
		return canRename();
	}

	/** {@inheritDoc} */
	public boolean canRename() {
		File parent = file.getParentFile();
		if ("..".equals(resourcePath) || ".".equals(resourcePath) ||
				(! file.exists()) ||
				parent == null || (! parent.canWrite()) ||
				Arrays.asList(File.listRoots()).contains(file)) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	public boolean canCompress() {
		if (! canRename()) {
			return false;
		}
		if (isFile()) {
			File target = new File(getCanonicalResource()+".gz");
			String resourcePathLowerCase = resourcePath.toLowerCase();
//			String contentType = getContentType(servletContext);
			return file.isFile() && file.canRead() && file.length() > GZIP_MIN_FILE_SIZE && ! target.exists() &&
//				isCompressableMimeType(contentType) &&
				! resourcePathLowerCase.endsWith(".gz") &&
				! resourcePathLowerCase.endsWith(".tgz") &&
				! resourcePathLowerCase.endsWith(".bz2") &&
				! resourcePathLowerCase.endsWith(".tbz") &&
				! resourcePathLowerCase.endsWith(".xz") &&
				! resourcePathLowerCase.endsWith(".txz") &&
				! resourcePathLowerCase.endsWith(".7z") &&
				! resourcePathLowerCase.endsWith(".zip") &&
				! resourcePathLowerCase.endsWith(".jar") &&
				! resourcePathLowerCase.endsWith(".war") &&
				! resourcePathLowerCase.endsWith(".rar") &&
				! resourcePathLowerCase.endsWith(".ear");//TODO should test for more un-compressible extensions!
		} else if (isDirectory()) {
			File target = new File(getCanonicalResource()+".zip");
			return file.isDirectory() && file.canRead() && ! target.exists();
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	public InputStream getResourceAsStream(ServletContext servletContext) {
		try {
			return new BufferedInputStream(new FileInputStream(file), 8192);
		} catch (FileNotFoundException fnfe) {
			return null;
		} catch (SecurityException se) {
			return null;
		}
	}

	/** {@inheritDoc} */
	protected URL getURL(ServletContext context) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException mue) {
			return null;
		}
	}

	/** {@inheritDoc} */
	public long getContentLength(ServletContext context) {
		return file.length();
	}

	/** {@inheritDoc} */
	public long getLastModified(ServletContext context) {
		return file.lastModified();
	}


	/** {@inheritDoc} */
	public boolean delete() throws IOException {
		// delete the required file
		if (file.isFile()) {
			return file.delete();
		} else if (file.isDirectory()) {
			deleteDirectoryInternal(file);
		}
		return true;
	}

	protected boolean deleteDirectoryInternal(File dir) {
		// recursively delete the directory
		File[] children = dir.listFiles();
		for (int i = 0; i < children.length; ++i) {
			File child = children[i];
			if (child.isFile()) {
				child.delete();
			} else {
				deleteDirectoryInternal(child);
			}
		}
		return dir.delete();
	}

	/** {@inheritDoc} */
	public boolean compress() throws IOException {
		if (file.isFile()) {
			GZipUtils.compress(file);
		} else if (file.isDirectory()) {
			ZipUtils.compress(file);
			// Do not automatically delete the directory, as files may have been skipped during compression (e.g. read permission)!
		}
		return true;
	}

	/** {@inheritDoc} */
	public boolean renameTo(String newName) throws IOException {
		// rename the required file
		File newFile = new File(newName);
		return file.renameTo(newFile);
	}
}
