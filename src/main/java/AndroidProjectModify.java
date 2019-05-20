/*
作用：
1. 将jacoco模块放入项目
2. 在部分文件中添加jacoco模块调用的代码
3. 修改activity文件，包括头文件的增加与on系列函数的修改
 */

import java.io.*;

public class AndroidProjectModify {
    /**
     * @param codeDir
     * @param appDir
     * @param packageName
     * @throws IOException
     * @throws InterruptedException
     */
    public static void putJacocoGradle(String codeDir, String appDir, String packageName) throws IOException, InterruptedException {
        //将Jacoco文件放入App文件夹中
        BufferedReader br = null;
        BufferedWriter bw = null;
        String line = null;
        String filename = null;
        StringBuffer buf = new StringBuffer();
        File directory = new File("");
        String courseFile = directory.getCanonicalPath();
        System.out.println("courseFile: " + courseFile);
        Runtime r = Runtime.getRuntime();

        //不是相对路径就用绝对路径
        File f = new File(appDir);
        //app文佳佳地址
        if (!f.exists()) {
            appDir = courseFile + File.separator + appDir;
        }
        f = new File(codeDir);
        if (!f.exists()) {
            codeDir = courseFile + File.separator + codeDir;
        }


        if (OSUtil.isWin()) {
            String cmd = "Commands\\win\\putGradle.bat " + courseFile + " " + appDir + " " + codeDir;
            r.exec(cmd);
            Thread.sleep(5000);
            System.out.println("pull success");
        } else {

            System.out.println("bash " + "Commands/linux/putGradle.sh " + courseFile + " " + appDir + " " + codeDir);
            r.exec("bash " + "Commands/linux/putGradle.sh " + courseFile + " " + appDir + " " + codeDir);
            Thread.sleep(5000);
            System.out.println("pull success");
        }
        filename = codeDir + File.separator + "jacocotest.java";
        f = new File(filename);
        if (!f.exists()) {
            filename = courseFile + File.separator + filename;
        }
        //插入包名
        //理论上不超过2G不会溢出
        br = new BufferedReader(new FileReader(filename));
        buf.append("package " + packageName + ";");
        while ((line = br.readLine()) != null) {
            buf.append(line).append("\n");
        }
        br.close();
        bw = new BufferedWriter(new FileWriter(filename));
        bw.write(buf.toString());
        bw.close();
        buf = new StringBuffer();

        filename = codeDir + File.separator + "LogUtils.java";
        f = new File(filename);
        if (!f.exists()) {
            filename = courseFile + File.separator + filename;
        }
        br = new BufferedReader(new FileReader(filename));
        //插入包名
        //理论上不超过2G不会溢出
        buf.append("package " + packageName + ";");
        while ((line = br.readLine()) != null) {
            buf.append(line).append("\n");
        }
        br.close();
        bw = new BufferedWriter(new FileWriter(filename));
        bw.write(buf.toString());
        bw.close();
    }

    public static boolean isContain(File f, String contain) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(contain)) {
                br.close();
                return true;
            }
        }
        br.close();
        return false;
    }

    public static boolean[] isContainOn(File f) throws IOException {
        //0 = import, 1 = onCreate(, 2 = onPause(, 3 = onDestroy(, 4 = onResume(
        boolean[] res = new boolean[5];

        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(".jacocotest")) {
                res[0] = true;
            } else if (line.contains("onCreate(")) {
                res[1] = true;
            } else if (line.contains("onPause(")) {
                res[2] = true;
            } else if (line.contains("onDestroy(")) {
                res[3] = true;
            } else if (line.contains("onResume(")) {
                res[4] = true;
            }
        }
        br.close();
        return res;
    }

    public static void modifyBuild(File f) throws IOException {
        BufferedReader br = null;
        BufferedWriter bw = null;
        String line = "";
        StringBuffer buf = new StringBuffer();
        File directory = new File("");
        String courseFile = directory.getCanonicalPath();
        System.out.println(courseFile);
        boolean containApply = isContain(f, "testCoverageEnabled = true");
        boolean containFlags = isContain(f, "options.compilerArgs << \"-Xlint:unchecked\" << \"-Xlint:deprecation\"");
        if (!f.exists()) {
            f = new File(courseFile + File.separator + f.getName());
        }

        br = new BufferedReader(new FileReader(f));
        if (!containApply) buf.append("apply from: 'jacoco.gradle'\n");
        while ((line = br.readLine()) != null) {
            if (line.contains("buildTypes") && line.contains("{")) {
                if (!containApply) {
                    containApply = true;
                    buf.append(line).append("\n");
                    buf.append("\tdebug {\n\t\ttestCoverageEnabled = true\n\t}\n");
                } else buf.append(line).append("\n");
            } else if (line.contains("android") && line.contains("{")) {
                if (!containFlags) {
                    containFlags = true;
                    buf.append(line).append("\n");
                    buf.append("    lintOptions {\n" +
                            "        checkReleaseBuilds false\n" +
                            "        abortOnError false\n" +
                            "    }\n" +
                            "    allprojects {\n" +
                            "        gradle.projectsEvaluated {\n" +
                            "            tasks.withType(JavaCompile) {\n" +
                            "                options.compilerArgs << \"-Xlint:unchecked\" << \"-Xlint:deprecation\"\n" +
                            "            }\n" +
                            "        }\n" +
                            "    }\n");
                } else buf.append(line).append("\n");
            } else buf.append(line).append("\n");
        }
        br.close();
        //write
        bw = new BufferedWriter(new FileWriter(f));
        bw.write(buf.toString());
        bw.close();
    }

    public static void modifyActivity(File f, String packageName) throws IOException {
        String courseFile = f.getCanonicalPath();
        BufferedReader br = null;
        BufferedWriter bw = null;
        String line = null;
        StringBuffer buf = null;

        //instrument
        br = new BufferedReader(new FileReader(f));
        buf = new StringBuffer();
        //查看源文件是否拥有import,onCreate(,onPause(,onDestroy(,onResume(
        boolean[] isContainOn = isContainOn(f);
        int importFlag = 0;
        //read
        if (isContainOn[0]) return;
        while ((line = br.readLine()) != null) {
            //TODO 没有import就……应该不会吧
            if (importFlag == 0 && line.contains("import")) {
                importFlag = 1;
                buf.append(line).append("\n");
                buf.append("import " + packageName + ".jacocotest;\n");
            }
            //根据isContainOn,在一开始加上函数覆写
            else if (line.contains("public class ")) {
                while (!line.contains("{")) {
                    buf.append(line).append("\n");
                    line = br.readLine();
                }
                buf.append(line).append("\n");
                //0 = import, 1 = onCreate(, 2 = onPause(, 3 = onDestroy(, 4 = onResume(
                if (!isContainOn[1]) {
                    buf.append("protected void onCreate(){").append("\n");
                    buf.append(" super.onCreate();").append("\n");
                    buf.append("jacocotest.generateEcFile(false);\n").append("}\n");

                }
                if (!isContainOn[2]) {
                    buf.append("protected void onPause(){").append("\n");
                    buf.append(" super.onPause();").append("\n");
                    buf.append("jacocotest.generateEcFile(false);\n").append("}\n");
                }
                if (!isContainOn[3]) {
                    buf.append("protected void onDestroy(){").append("\n");
                    buf.append(" super.onDestroy();").append("\n");
                    buf.append("jacocotest.generateEcFile(false);\n").append("}\n");
                }
                if (!isContainOn[4]) {
                    buf.append("protected void onResume(){").append("\n");
                    buf.append(" super.onResume();").append("\n");
                    buf.append("jacocotest.generateEcFile(false);\n").append("}\n");
                }

            }
            //TODO 只有一句super不加大括号的，是傻逼应用，不理
            else if ((line.contains(" onCreate(") || line.contains(" onPause(")
                    || line.contains(" onDestroy(") || line.contains(" onResume("))
                    && (line.contains("public") || line.contains("protected") || line.contains("private"))
                    && line.contains("void")) {
                while (!line.contains("{")) {
                    buf.append(line).append("\n");
                    line = br.readLine();
                }
                //可能直接一句结束
                if (line.split("\\{}").length > 2) {
                    System.out.println("what sentence??? more than two } in one public class");
                    buf.append(line).append("\n");
                } else {
                    line = line.split("\\{")[0] + "{";
                    String suffix = "";
                    if (line.split("\\{").length == 2) {
                        suffix = line.split("\\{")[1];
                    }
                    buf.append(line).append("\n");
                    buf.append("jacocotest.generateEcFile(false);\n");
                    buf.append(suffix).append("\n");
                }
            } else buf.append(line).append("\n");
        }
        //write
        bw = new BufferedWriter(new FileWriter(f));
        bw.write(buf.toString());
        bw.close();
    }


    public static void main(String args[]) throws IOException, InterruptedException {
        String codedir, appdir, dir;
        String build1, build2, maindir;
        String packageName;
        //appdir=the path of the 'app' document in your project
        //appCodedir=the path of the 'java' dir in 'app'
        //args:appDir,appCodeDir,packagename

        //Example:
        //bihudaily
        //codedir="/home/xyr/eclipse-workspace/BihuDaily-master/app/src/main/java/com/white/bihudaily/";
        //appdir="/home/xyr/eclipse-workspace/BihuDaily-master/app/";
        //setting="/home/xyr/eclipse-workspace/BihuDaily-master/settings.gradle";
        //maindir="/home/xyr/eclipse-workspace/BihuDaily-master/app/src/main/java/";
        //packageName="com.white.bihudaily";

        dir = args[0];
        maindir = args[1];
        packageName = args[2];
        codedir = maindir + File.separator + packageName.replaceAll(".", File.separator) + File.separator;
        appdir = dir + "app/";

        putJacocoGradle(codedir, appdir, packageName);
        build1 = appdir + "build.gradle";
        build2 = appdir + "build.gradle";
        // modifyFile("build",build1);
        // modifyFile("build2",build2);
        File f = new File(maindir);
        // modifyFile(f,packageName);
    }


}
