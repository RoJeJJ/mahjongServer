package com.buding.common.util;

import java.io.*;

public class IOUtil {
	public static String getClassPathResourceAsString(String path, String encoding) throws Exception {
		InputStream stream = IOUtil.class.getResourceAsStream(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}
	
	public static byte[] tryGetFileData(String filePath) throws Exception {
		if(new File(filePath).exists()) {
			return getFileData(filePath);
		}
		return null;
	}
	
	public static byte[] getFileData(String filePath) throws Exception {
		FileInputStream fin = new FileInputStream(filePath);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			int size = 1024;
			byte buff[] = new byte[size];
			while((size = fin.read(buff)) != -1) {
				out.write(buff, 0, size);
			}
		} catch (Exception e){
			e.getLocalizedMessage();
		}finally {
			fin.close();
		}
		
		return out.toByteArray();
	}
	
	public static String getFileResourceAsString(File file, String encoding) throws Exception {
		InputStream stream = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = reader.readLine()) != null) {
			sb.append(line).append("\r\n");
		}
		return sb.toString();
	}
	
	public static String getFileResourceAsString(String file, String encoding) throws Exception {
		return getFileResourceAsString(new File(file), encoding);
	}
	
	public static File[] listFiles(String folder) {
		File f = new File(folder);
		return f.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				//不要隐藏文件
				return !pathname.getName().startsWith(".");
			}
		});
	}
	
	public static void writeFileContent(String filePath, String content) throws Exception {
		File file = new File(filePath);
		if(file.getParentFile() != null && file.getParentFile().exists() == false) {
			file.getParentFile().mkdirs();
		}
		FileOutputStream fout = new FileOutputStream(filePath);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(fout, "UTF-8"));
		writer.println(content);
		writer.flush();
		writer.close();
		return;
	}

	/**
	 * 删除某个文件夹下的所有文件夹和文件
	 *
	 * @param delPath String
	 * @return boolean
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean deletefile(String delPath) throws Exception {
		try {
			File file = new File(delPath);
			if (!file.isDirectory()) {
				file.delete();
			} else if (file.isDirectory()) {
				String[] fileList = file.list();
				for (int i = 0; i < fileList.length; i++) {
					File delFile = new File(delPath + "\\" + fileList[i]);
					if (!delFile.isDirectory()) {
						delFile.delete();
						System.out.println(delFile.getAbsolutePath() + " 删除文件成功");
					} else if (delFile.isDirectory()) {
						deletefile(delPath + "\\" + fileList[i]);
					}
				}
				System.out.println(file.toString() + " 删除文件夹");
				file.delete();
			}

		} catch (FileNotFoundException e) {
			System.out.println("deletefile() Exception:" + e.getMessage());
		}
		return true;
	}
}
