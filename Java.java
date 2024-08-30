for (int var6 = 0; var6 < var5; ++var6) {
    String fileName = var4[var6];
    fileName = fileName.replace("\\", "/");

    // 使用 BufferedInputStream 包装 FileInputStream 以提高性能
    try (InputStream inputStream = new BufferedInputStream(new FileInputStream(fileName))) {
        sftp.put(inputStream, fileName.substring(fileName.lastIndexOf('/') + 1));
    } catch (IOException e) {
        // 处理 FileInputStream 的 IO 异常
        e.printStackTrace();
    } catch (SftpException e) {
        // 处理 SFTP 异常
        e.printStackTrace();
    }
}
