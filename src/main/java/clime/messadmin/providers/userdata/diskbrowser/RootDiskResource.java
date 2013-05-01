/**
 *
 */
package clime.messadmin.providers.userdata.diskbrowser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletContext;

import clime.messadmin.providers.userdata.resourcebrowser.BaseResource;

/**
 * FileSystem root.
 * This class is necessary as a filesystem may have multiple roots.
 *
 * @author C&eacute;drik LIME
 */
class RootDiskResource extends BaseResource {
	protected static final RootDiskResource INSTANCE = new RootDiskResource();

	private RootDiskResource() {
		super("");
	}

	/** {@inheritDoc} */
	public BaseResource getCanonicalResource() {
		return this;
	}

	/** {@inheritDoc} */
	public Collection/*<DiskResource>*/ getChildResources(ServletContext context) {
		File[] roots = File.listRoots();
		Collection/*<DiskResource>*/ result = new ArrayList(roots == null ? 1 : roots.length);
		if (roots != null) {
			if (roots.length == 0) {
				result.add(new DiskResource(new File("").getPath()));
			} else {
				for (int i = 0; i < roots.length; ++i) {
					File root = roots[i];
					// roots[i].getCanonicalPath() leads to a "drive not ready" on Windows...
					result.add(new DiskResource(root.getPath()));
				}
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	protected BaseResource getParentDirectoryInternal() {
		return null;
	}

	/** {@inheritDoc} */
	public String getFileName() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	public boolean isFile() {
		return false;
	}
	/** {@inheritDoc} */
	public boolean isDirectory() {
		return true;
	}

	/** {@inheritDoc} */
	public boolean isHidden() {
		return false;
	}

	/** {@inheritDoc} */
	public boolean canRead() {
		return true;
	}
	/** {@inheritDoc} */
	public boolean canWrite() {
		return false;
	}

	/** {@inheritDoc} */
	public boolean canDelete() {
		return false;
	}

	/** {@inheritDoc} */
	public boolean canRename() {
		return false;
	}

	/** {@inheritDoc} */
	public boolean canCompress() {
		return false;
	}

	/** {@inheritDoc} */
	public InputStream getResourceAsStream(ServletContext servletContext) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	protected URL getURL(ServletContext context) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	public long getContentLength(ServletContext context) {
		return 0;
	}

	/** {@inheritDoc} */
	public long getLastModified(ServletContext context) {
		return 0;
	}


	/** {@inheritDoc} */
	public boolean delete() throws IOException {
		throw new UnsupportedOperationException();
	}


	/** {@inheritDoc} */
	public boolean compress() throws IOException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	public boolean renameTo(String newName) throws IOException {
		throw new UnsupportedOperationException();
	}
}
