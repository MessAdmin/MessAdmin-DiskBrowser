/**
 *
 */
package clime.messadmin.providers.userdata.diskbrowser;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.providers.spi.DisplayProvider;
import clime.messadmin.providers.userdata.resourcebrowser.BaseBrowserHelper;
import clime.messadmin.providers.userdata.resourcebrowser.BaseResource;
import clime.messadmin.utils.BytesFormat;
import clime.messadmin.utils.Files;
import clime.messadmin.utils.StringUtils;

/**
 * Browse the FileSystem
 * @author C&eacute;drik LIME
 */
class DiskBrowserHelper extends BaseBrowserHelper {
	private static final String BUNDLE_NAME = DiskBrowser.class.getName();

	public DiskBrowserHelper(AdminActionProvider adminActionProviderCallback, DisplayProvider displayProviderCallback) {
		super(adminActionProviderCallback, displayProviderCallback);
	}

	/** {@inheritDoc} */
	@Override
	public String getI18nBundleName() {
		return BUNDLE_NAME;
	}

	/** {@inheritDoc} */
	@Override
	protected BaseResource getResource(ServletContext context, String resourcePath) {
		if ("".equals(resourcePath)) {
			return getDefaultRootResource();
		} else {
			return new DiskResource(resourcePath);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected BaseResource getDefaultRootResource() {
		return RootDiskResource.INSTANCE;
	}

	/** {@inheritDoc} */
	@Override
	protected BaseResource getDefaultUserResource() {
		try {
//			return FileSystemView.getFileSystemView().getDefaultDirectory().getCanonicalPath();
			return new DiskResource(new File("").getCanonicalPath());
		} catch (IOException e) {
			return getDefaultRootResource();
		}
	}

	/** {@inheritDoc} */
	@Override
	protected String getXHTMLPreResourceListing(BaseResource resource) {
		StringBuffer out = new StringBuffer(512);
		out.append(super.getXHTMLPreResourceListing(resource));
		if ( ! RootDiskResource.INSTANCE.equals(resource)) {//StringUtils.isNotBlank(resource.getPath())
			// display disk free space (if available)
			{
				long diskUseableSpace = Files.getUsableSpaceForFile(((DiskResource)resource).file);
				if (diskUseableSpace >= 0) {
					BytesFormat format = BytesFormat.getBytesInstance(I18NSupport.getAdminLocale(), true);
					String result = I18NSupport.getLocalizedMessage(getI18nBundleName(), "xhtml.pre",//$NON-NLS-1$
							new Object[] {format.format(diskUseableSpace)});
					out.append(result).append('\n');
				}
			}
		}
		// Form for mass actions
		{
			String massActionFormName = adminActionProviderCallback.getActionID()+"-massActions";
			out.append("<form id=\"").append(massActionFormName).append("\" action=\"?").append(AdminActionProvider.ACTION_PARAMETER_NAME).append('=').append(adminActionProviderCallback.getActionID()).append("\" method=\"post\">\n");
			//out.append("<input type=\"hidden\" name=\"").append(AdminActionProvider.ACTION_PARAMETER_NAME).append("\" value=\"").append(adminActionProviderCallback.getActionID()).append("\"/>\n");
			out.append("<input type=\"hidden\" name=\"").append(FILE_ACTION_PARAMETER_NAME).append("\" id=\"").append(massActionFormName+'_'+FILE_ACTION_PARAMETER_NAME).append("\" value=\"\"/>");
		}
		return out.toString();
	}

	/** {@inheritDoc} */
	@Override
	protected String getXHTMLPostResourceListing(BaseResource resource) {
		StringBuilder out = new StringBuilder(1024);
		// Form for mass actions
		// "check all" checkbox
		String massActionFormName = adminActionProviderCallback.getActionID()+"-massActions";
		out.append("<ul style=\"margin: 0\"><li style=\"list-style-type: none;\"><label><input type=\"checkbox\" onclick=\"javascript:checkUncheckAllCB(this, '").append(getResourceID()).append("');\"/>");
		out.append(I18NSupport.getLocalizedMessage(getI18nInternalBundleName(), "select.all"));//$NON-NLS-1$
		out.append("</label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		{
			// mass deletion
			String urlPrefix = "?" + AdminActionProvider.ACTION_PARAMETER_NAME + '=' + adminActionProviderCallback.getActionID()
				+ '&' + FILE_ACTION_PARAMETER_NAME + '=' + FILE_DELETE_ACTION;
			out.append("<button name=\"submit\" onclick=\"if (hasCheckedCB(this, '").append(getResourceID()).append("') && window.confirm('").append(I18NSupport.getLocalizedMessage(getI18nBundleName(), "action.delete.confirm")).append("'))  jah('").append(urlPrefix).append("','").append(DisplayProvider.Util.getId(displayProviderCallback)).append("','POST',buildFormQueryString('").append(massActionFormName).append("'));return false;\">");
			out.append(I18NSupport.getLocalizedMessage(getI18nInternalBundleName(), "action.delete"));//$NON-NLS-1$
			out.append("</button>\n");
		}
		{
			// mass compression
			String urlPrefix = "?" + AdminActionProvider.ACTION_PARAMETER_NAME + '=' + adminActionProviderCallback.getActionID()
				+ '&' + FILE_ACTION_PARAMETER_NAME + '=' + FILE_COMPRESS_ACTION;
			out.append("<button name=\"submit\" onclick=\"if (hasCheckedCB(this, '").append(getResourceID()).append("') && window.confirm('").append(I18NSupport.getLocalizedMessage(getI18nBundleName(), "action.compress.confirm", null)).append("'))  jah('").append(urlPrefix).append("','").append(DisplayProvider.Util.getId(displayProviderCallback)).append("','POST',buildFormQueryString('").append(massActionFormName).append("'));return false;\">");
			out.append(I18NSupport.getLocalizedMessage(getI18nInternalBundleName(), "action.compress"));//$NON-NLS-1$
			out.append("</button>\n");
		}
		{
			// mass download
			out.append("<button name=\"submit\" onclick=\"if (! hasCheckedCB(this, '").append(getResourceID()).append("'))  return false; { document.getElementById('").append(massActionFormName).append("').target='_blank'; document.getElementById('").append(massActionFormName+'_'+FILE_ACTION_PARAMETER_NAME).append("').value='").append(FILE_DOWNLOAD_ACTION).append("'; return true; }\">");
			out.append(I18NSupport.getLocalizedMessage(getI18nInternalBundleName(), "action.download"));//$NON-NLS-1$
			out.append("</button>\n");
		}
		out.append("</li></ul>\n");
		out.append("</form>\n");
		out.append(super.getXHTMLPostResourceListing(resource));
		return out.toString();
	}

	/** {@inheritDoc} */
	@Override
	protected void appendEntryPre(StringBuffer xhtml, ServletContext context, BaseResource path, String displayName, String urlPrefix) {
		if ( ! "..".equals(displayName) && ! RootDiskResource.INSTANCE.equals(path) && ! Arrays.asList(File.listRoots()).contains(((DiskResource)path).file)) {
			// Form for mass actions (deletion & compression for now)
			boolean enabled = path.canDelete() || path.canCompress();
			xhtml.append("<input type=\"checkbox\" name=\"").append(getResourceID()).append("\" value=\"").append((path.getParentDirectory()==null?"":StringUtils.escapeXml(path.getPath()))).append('"');
			if ( ! enabled) {
				//xhtml.append(" style=\"visibility: hidden\"");
				xhtml.append(" disabled=\"disabled\"");
			}
			xhtml.append("/>");
		}
		super.appendEntryPre(xhtml, context, path, displayName, urlPrefix);
	}

	/** {@inheritDoc} */
	@Override
	protected String getXHTMLResourceListing(ServletContext context, BaseResource resource) {
		return super.getXHTMLResourceListing(context, resource.getCanonicalResource());
	}

	/** {@inheritDoc} */
	@Override
	protected boolean compressFile(BaseResource resource, HttpServletRequest request, HttpServletResponse response, ServletContext context) throws IOException {
		boolean result = resource.compress();
		resource.delete();
		return result;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean compressDirectory(BaseResource resource, HttpServletRequest request, HttpServletResponse response, ServletContext context) throws IOException {
		boolean result = resource.compress();
		// Do not automatically delete the directory, as files may have been skipped during compression (e.g. read permission)!
		return result;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean renameFile(BaseResource resource, String newName, HttpServletRequest request, HttpServletResponse response, ServletContext context) throws IOException {
		// rename the required file
		File newFile = new File(newName);
		if (newFile.exists()) {
			response.sendError(HttpServletResponse.SC_CONFLICT, StringUtils.escapeXml(newName));
			return false;
		}
		resource.renameTo(newName);
		return true;
	}
}
