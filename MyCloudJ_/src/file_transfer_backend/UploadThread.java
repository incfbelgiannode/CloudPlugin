package file_transfer_backend;

import ij.io.Opener;

import java.io.File;
import java.util.concurrent.Callable;

import CloudGui.CloudFileTree;
import CloudGui.Logger;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;

public class UploadThread implements Callable<Void> {
	private CloudOperations cloudHandler;
	private Logger logger;
	private CloudFileTree cloudFileTree;
	private TransferTask task;

	public UploadThread(TransferTask task, CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, Logger logger) {
		this.cloudHandler = cloudHandler;
		this.logger = logger;
		this.cloudFileTree = cloudFileTree;
		this.task = task;
	}

	@Override
	public Void call() throws CloudException {
		String sourcePath = "";
		String destPath = "";
		String uploadType = ""; // file/folder

		try {
			// save task parameters
			sourcePath = task.getSourcePath();
			destPath = task.getDestinationPath();
			File file = new File(sourcePath);
			boolean isFileUpload = file.isFile();
			uploadType = isFileUpload ? "file" : "folder";

			// start the upload
			if (isFileUpload)
				cloudHandler.uploadFile(sourcePath, destPath);
			else
				cloudHandler.uploadFolder(sourcePath, destPath);
			logger.writeLog("Uploading of " + sourcePath + " complete \n\n");

			if (file.isFile())
				openFile(task);
		} catch (CloudException e) {
			e.printStackTrace();
			logger.writeLog("Error uploading " + uploadType + " " + sourcePath
					+ ". " + e.getCloudError() + "\n\n");
			return null;
		}

		// update the file browsing tree with the new node
		cloudFileTree.updateTrees(destPath, true);

		// TODO: in the future we'll return the file transfer status
		return null;
	}

	private void openFile(TransferTask task) {
		// Open in the default application
		Opener openfile = new Opener();
		openfile.open(task.getSourcePath());
	}

	public TransferTask getTask() {
		return task;
	}

	public void setTask(TransferTask task) {
		this.task = task;
	}
}
