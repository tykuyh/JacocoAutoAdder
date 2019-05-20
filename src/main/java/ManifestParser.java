/*
input: project path, app path
output: activity path of app of this project

 */

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ManifestParser {
    //输入项目地址与存放activity的app地址
    //输出该app地址下包含的所有activity地址(所有信息？)
    //TODO 目前只支持java，kotlin需要更多特殊处理

    /**
     *
     * @param projectPath
     * @param appPath
     * @return
     */
    public static List<String> getActivityPath(String projectPath, String appPath) {
        String filePath = null;
        if (!appPath.contains(projectPath)) {
            System.out.println("App文件夹路径不完整，请输入包含项目路径的App文件夹路径");
            return null;
        }
        //找到Manifest地址
        String appPathProfix = appPath + File.separator + "src" + File.separator + "main" + File.separator;
        filePath = appPathProfix+ "AndroidManifest.xml";
        File ftest = new File(filePath);
        if(!ftest.exists()){
            System.out.println("AndroidManifest位置不正确，请将地址作为第三个参数输入本函数");
            return null;
        }
        //解析
        AndroidManifestAnalyze a = new AndroidManifestAnalyze();
        a.xmlHandle(filePath);
        //信息都在a里，此处只拿activity
        List<String> activities = a.getActivities();
        //转换成针对项目的相对路径地址
        List<String> res = new ArrayList();
        Iterator it= activities.iterator();
        while(it.hasNext()){
            String tempIt=((String)it.next());
            if(OSUtil.isWin())
                tempIt=tempIt.replaceAll("\\.","\\\\");
            else tempIt=tempIt.replaceAll("\\.","/");
            res.add(appPathProfix+"java"+File.separator+tempIt+".java");
        }
        return res;
    }

    //app名字有问题的暂时使用直接输入的方式
    public static List<String> getActivityPath(String projectPath, String appPath, String activityPath) {
        //TODO 未测试
        int index=activityPath.lastIndexOf('.');
        String appPathProfix = activityPath.substring(0,index);
        //解析
        AndroidManifestAnalyze a = new AndroidManifestAnalyze();
        a.xmlHandle(activityPath);
        //信息都在a里，此处只拿activity
        List<String> activities = a.getActivities();
        //转换成针对项目的相对路径地址
        List<String> res = new ArrayList();
        Iterator it= activities.iterator();
        while(it.hasNext()){
            String tempIt=((String)it.next());
            if(OSUtil.isWin())
                tempIt=tempIt.replaceAll("\\.","\\\\");
            else tempIt=tempIt.replaceAll("\\.","/");
            res.add(appPathProfix+"java"+File.separator+tempIt);
        }
        return res;
    }
    //输入apk地址
    //输出apk包含的所有activity名称
    public static ArrayList<String> getActivityPath(String apkPath) {
        ArrayList<String> res = null;
        //TODO
        return res;
    }

    public static void main(String arg[]) {
        List<String> res = getActivityPath("E:\\experiment2\\App_For_Instrument\\rootCode\\Graphics\\memetastic",
                "E:\\experiment2\\App_For_Instrument\\rootCode\\Graphics\\memetastic\\app");
        System.out.println("success");
    }
}