import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import java.util.List;

public class Main {
    public static void main(String args[]) throws IOException, InterruptedException {
        String projectPath = "D:\\App_For_Instrument\\alreadyCode\\Graphics\\memetastic";
        String appPath = "D:\\App_For_Instrument\\alreadyCode\\Graphics\\memetastic\\app";
        String packageName = "net.gsantner.memetastic";
        addJacocoFiles(projectPath,appPath,packageName);
    }
    public static void addJacocoFiles(String projectPath, String appPath, String packageName) throws IOException, InterruptedException {

        String mainPath = appPath+File.separator+"src"+File.separator+"main"+File.separator+"java"+File.separator;
        String codePath = mainPath+File.separator+packageName.replaceAll("\\.", "\\\\")+File.separator;
        //从Manifest中提取信息,获取Activity的相对路径
        List<String> res = ManifestParser.getActivityPath(projectPath,appPath);
        //根据项目名获取Activity的绝对路径
        String appPathProfix = appPath + File.separator + "src" + File.separator + "main" + File.separator;
        String filePath = appPathProfix+ "AndroidManifest.xml";
        //找到入口Activity
        String s =AndroidManifestAnalyze.findLaucherActivity(filePath);
        System.out.println(s);

        //放置Jacoco获取插桩数据的代码
        AndroidProjectModify.putJacocoGradle(codePath,appPath,packageName);
        //修改build
        AndroidProjectModify.modifyBuild(new File(appPath+File.separator+"build.gradle"));
        //修改所有Activity
        for(int i=0;i<res.size();i++){
            File f = new File(res.get(i));
            AndroidProjectModify.modifyActivity(f,packageName);
        }
    }
}
