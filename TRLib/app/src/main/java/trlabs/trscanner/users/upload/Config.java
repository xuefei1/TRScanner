package trlabs.trscanner.users.upload;

import trlabs.trscanner.trtabs.config.GlobalConsts;

public class Config {
	// File upload url (replace the ip with your server address)
	public static final String FILE_UPLOAD_URL = GlobalConsts.SERVER_ADDR + GlobalConsts.PHP_UPLOAD;
	
	// Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "";
}
