import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class NexusComponentsUpload {

    public static final String USAGE =
            "Expected Parameters: [options] -U username -P password -h host -p port -r repositoryName -d repositoryDirectory\n" +
                    "\t-U - nexus username\n" +
                    "\t-P - nexus password\n" +
                    "\t-h - nexus host\n" +
                    "\t-p - nexus port\n" +
                    "\t-r - nexus repository name\n" +
                    "\t-d - local repository directory\n";

    public static void main(String[] args) {
        NexusComponentsUpload nexusComponentsUpload = new NexusComponentsUpload();

        String repositoryHost = "localhost";
        String repositoryPort = "8081";
        String repositoryName = "maven-hosted";
        String repositoryDirectory = "~/repo/";
        String username = "admin";
        String password = "admin123";

        int base = 0;
        for (base = 0; base < args.length; base++) {
            if (args[base].equals("-h")) {
                repositoryHost = args[++base];
            } else if (args[base].equals("-p")) {
                repositoryPort = args[++base];
            } else if (args[base].equals("-r")) {
                repositoryName = args[++base];
            } else if (args[base].equals("-d")) {
                repositoryDirectory = args[++base];
            } else if (args[base].equals("-U")) {
                username = args[++base];
            } else if (args[base].equals("-P")) {
                password = args[++base];
            } else {
                break;
            }
        }

        int minParams = 1;
        int remain = args.length;
        if (remain < minParams) {
            if (args.length > 0) {
                System.err.println("Actual Parameters: " + Arrays.toString(args));
            }
            System.err.println(USAGE);
            System.exit(1);
        }

        System.out.println("Repository:" + repositoryName);
        System.out.println("Repository:" + repositoryName);

        // 获取所有的pom文件
        ArrayList<String> files = getFileLists(repositoryDirectory, ".pom");

        // 一个个的解析并处理POM文件及对应的jar包
        for (String file : files) {
            try {

                String fileNameWithoutExtension = file.substring(0, file.lastIndexOf("."));

                // 解析POM文件
                GAVP gavp = nexusComponentsUpload.parsePom(file);
                //如果是POM组件,直接上传
                if (gavp.packaging.equals("pom")) {
                    String cmd = getApiCmd(repositoryHost, repositoryPort, username, password, repositoryName, gavp.groupId, gavp.artifactId, gavp.version, "pom", file);
                    exec(cmd);
                } else {
                    //如果是jar包组件,判断一下jar包是存在
                    String jarFilePath = fileNameWithoutExtension + ".jar";
                    File jarFile = new File(jarFilePath);
                    if (jarFile.exists()) {
                        String cmd = getApiCmd(repositoryHost, repositoryPort, username, password, repositoryName, gavp.groupId, gavp.artifactId, gavp.version, "jar", jarFilePath);
                        exec(cmd);
                    }
                }

            } catch (Exception e) {
                System.err.println("Error:"+e);
            }

        }

    }

    /**
     * 获取待执行的命令字符串
     * @param file
     * @return
     */
    public static String getApiCmd(String host, String port, String username, String password, String repository, String groupId, String artifactId, String version, String extension, String file) {
        String apiUri = "/service/rest/v1/components?repository=";
        String cmd = "curl -v -u " + username + ":" + password
                + " -X POST \'http://" + host + ":" + port + apiUri + repository + "\'"
                + " -F maven2.generate-pom=true -F maven2.groupId=" + groupId + " -F maven2.artifactId=" + artifactId + " -F maven2.version=" + version
                + " -F maven2.asset1=@" + file + " -F maven2.asset1.extension=" + extension;
        return cmd;
    }

    /**
     * 解析POM文件
     * @param file
     * @return
     */
    public GAVP parsePom(String file) {
        String fileContent = readFileString(file);
        //groupId
        String groupId = fileContent.substring(fileContent.indexOf("<groupId>")+9, fileContent.indexOf("</groupId>"));
        //artifactId
        String artifactId = fileContent.substring(fileContent.indexOf("<artifactId>")+12, fileContent.indexOf("</artifactId>"));
        //version
        String version = fileContent.substring(fileContent.indexOf("<version>")+9, fileContent.indexOf("</version>"));
        //packaging
        String packaging = "jar";
        if (fileContent.indexOf("<packaging>")>0) {
            packaging = fileContent.substring(fileContent.indexOf("<packaging>")+11, fileContent.indexOf("</packaging>"));
        }
        return new GAVP(groupId, artifactId, version, packaging);
    }

    /**
     * 执行命令
     * @param cmd
     */
    public static void exec(String cmd) {

        Process process = null;
        try {
            System.out.println("============exec cmd============");
            System.out.println(cmd);

            process = Runtime.getRuntime().exec("/bin/sh");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            bw.write(cmd, 0, cmd.length());
            bw.newLine();
            bw.write("exit", 0, 4);
            bw.newLine();
            bw.flush();

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                System.err.println(cmd+"\n"+e);
            }

            InputStream errIs = process.getErrorStream();
            InputStream stdIs = process.getInputStream();

            int len = errIs.available();
            byte[] buf = null;
            if (len != 0) {
                buf = new byte[len];
                System.err.println("============stderr msg============");
                errIs.read(buf);
                System.err.println(new String(buf, 0, len));
            }

            len = stdIs.available();
            if (len != 0) {
                buf = new byte[len];
                System.out.println("============stdout msg===========");
                stdIs.read(buf);
                System.out.println(new String(buf, 0, len));
            }
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 判断目录不存在
     * @param filePath
     * @return
     */
    public static boolean directoryExists(String filePath) {
        File file = new File(filePath);
        return file.isDirectory() && file.exists();
    }

    /**
     * 判断目录存在
     * @param filePath
     * @return
     */
    public static boolean directoryNotExists(String filePath) {
        return !directoryExists(filePath);
    }

    /**
     * 判断文件存在
     * @param filePath
     * @return
     */
    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.isFile() && file.exists();
    }

    /**
     * 判断文件不存在
     * @param filePath
     * @return
     */
    public static boolean fileNotExists(String filePath) {
        return !fileExists(filePath);
    }

    /**
     * 列出目录下所有的文件  */
    public static ArrayList<String> getFileLists(String directory, String extension) {
        ArrayList<String> fileLists = new ArrayList<>();

        if (directoryNotExists(directory)) {
            return fileLists;
        }

        File rootDirectory = new File(directory);
        File[] files = rootDirectory.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                // 文件
                if (file.getAbsolutePath().endsWith(extension) || extension == null || extension.length() == 0) {
                    fileLists.add(file.getAbsolutePath());
                }
            } else {
                // 子目录
                fileLists.addAll(getFileLists(file.getAbsolutePath(), extension));
            }
        }
        return fileLists;
    }

    public static String readFileString(String filePath) {
        StringBuffer fileString = new StringBuffer("");
        if (fileNotExists(filePath)) {
            return fileString.toString();
        }

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(filePath);
            bufferedReader = new BufferedReader(fileReader);
            String str;
            // read by line
            while ((str = bufferedReader.readLine()) != null) {
                fileString.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                }
            }
        }
        return fileString.toString();
    }

    /**
     * 存放POM信息
     */
    public class GAVP {
        public String groupId;
        public String artifactId;
        public String version;
        public String packaging;

        GAVP(String groupId,
             String artifactId,
             String version,
             String packaging) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.packaging = packaging;
        }

        @Override
        public String toString() {
            return "GAVP{" +
                    "groupId='" + groupId + '\'' +
                    ", artifactId='" + artifactId + '\'' +
                    ", version='" + version + '\'' +
                    ", packaging='" + packaging + '\'' +
                    '}';
        }
    }

}
