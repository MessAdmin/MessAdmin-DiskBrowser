/**
 *
 */
package clime.messadmin.providers.userdata.diskbrowser;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionProvider;
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.providers.spi.ServerDataProvider;

/**
 * Browse the FileSystem
 * @author C&eacute;drik LIME
 */
public class DiskBrowser extends BaseAdminActionProvider implements ServerDataProvider, AdminActionProvider {
	private static final String BUNDLE_NAME = DiskBrowser.class.getName();
	private static final String ACTION_ID = "browseDisk";//$NON-NLS-1$

	protected transient DiskBrowserHelper helper;

	public DiskBrowser() {
		super();
		helper = new DiskBrowserHelper(this, this);
	}

	/** {@inheritDoc} */
	@Override
	public int getPriority() {
		return 5;
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ACTION_ID;
	}

	/** {@inheritDoc} */
	public String getServerDataTitle() {
		return I18NSupport.getLocalizedMessage(BUNDLE_NAME, "title");//$NON-NLS-1$
	}

	/**
	 * This method will only be used for the initial display. Assume a {@code RESOURCE_ID} of {@code /}.
	 */
	public String getXHTMLServerData() {
		return helper.getXHTMLResourceListing(null);
	}

	/** {@inheritDoc} */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//setNoCache(response); // we don't want to prevent caching
		helper.serviceWithContext(request, response, null);
	}
}
