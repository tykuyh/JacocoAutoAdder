import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * AndroidManifest文件解析类
 *
 */

public class AndroidManifestAnalyze {

    private String appPackage;
    private List<String> permissions = new ArrayList();
    private List<String> activities = new ArrayList();

    public String getAppPackage() {
        return appPackage;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public List<String> getActivities() {
        return activities;
    }



    /**
     * 解析包名
     * @param doc
     * @return
     */
    public String findPackage(Document doc){
        NodeList nodeList = doc.getChildNodes();
        if(nodeList.getLength()==0) return null;
        Node node = nodeList.item(0);
        NamedNodeMap attrs  =node.getAttributes();
        int flag = 1;
        while(attrs==null&&flag<nodeList.getLength()){
            node = nodeList.item(flag);
            attrs  = node.getAttributes();
            flag++;
        }
        for(int i = 0; i < attrs.getLength(); i++){
            if(attrs.item(i).getNodeName() == "package"){
                return attrs.item(i).getNodeValue();
            }
        }
        return null;
    }

    /**
     * 解析入口activity
     * @param doc
     * @return
     */
    public static String findLaucherActivity(Document doc){
        Node activity = null;
        String sTem = "";
        NodeList categoryList = doc.getElementsByTagName("category");
        for(int i = 0; i < categoryList.getLength(); i++){
            Node category = categoryList.item(i);
            NamedNodeMap attrs  =category.getAttributes();
            for(int j = 0; j < attrs.getLength(); j++){
                if(attrs.item(j).getNodeName() == "android:name"){
                    if(attrs.item(j).getNodeValue().equals("android.intent.category.LAUNCHER")){
                        activity = category.getParentNode().getParentNode();
                        break;
                    }
                }
            }
        }
        if(activity != null){
            NamedNodeMap attrs  =activity.getAttributes();
            for(int j = 0; j < attrs.getLength(); j++){
                if(attrs.item(j).getNodeName() == "android:name"){
                    sTem = attrs.item(j).getNodeValue();
                }
            }
        }
        return sTem;
    }
    public static String findLaucherActivity(String filePath){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // 创建DocumentBuilder对象
            DocumentBuilder db = dbf.newDocumentBuilder();
            //加载xml文件
            Document document = db.parse(filePath);
            String s = findLaucherActivity(document);
            return s;
        }catch(Exception e){
            return null;
        }
    }
    /**
     * 解析入口
     * @param filePath
     */
    public  void xmlHandle(String filePath){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // 创建DocumentBuilder对象
            DocumentBuilder db = dbf.newDocumentBuilder();

            //加载xml文件
            Document document = db.parse(filePath);
            NodeList permissionList = document.getElementsByTagName("uses-permission");
            NodeList activityAll = document.getElementsByTagName("activity");

            //获取权限列表
            for (int i = 0; i < permissionList.getLength(); i++) {
                Node permission = permissionList.item(i);
                permissions.add((permission.getAttributes()).item(0).getNodeValue());
            }

            //获取activity列表
            appPackage = (findPackage(document));
            for(int i = 0; i < activityAll.getLength(); i++){
                Node activity = activityAll.item(i);
                NamedNodeMap attrs  =activity.getAttributes();
                for(int j = 0; j < attrs.getLength(); j++){
                    if(attrs.item(j).getNodeName() == "android:name"){
                        String sTem = attrs.item(j).getNodeValue();
                        if(sTem.startsWith(".")){
                            sTem = appPackage+sTem;
                        }
                        activities.add(sTem);
                    }
                }
            }
            String s = findLaucherActivity(document);
            if(s.startsWith(".")){
                s = appPackage+s;
            }
            //移动入口类至首位
            activities.remove(s);
            activities.add(0, s);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void output(AndroidManifestAnalyze a){
        System.out.println("packageName:"+a.appPackage);
        System.out.println("permissions("+a.permissions.size()+"):");
        for(int i = 0; i < a.permissions.size(); i++){
            System.out.println(a.permissions.get(i));
        }

        System.out.println("activities("+a.activities.size()+"):");
        for(int i = 0; i < a.activities.size(); i++){
            System.out.println(a.activities.get(i));
        }
    }
    public static void main(String[] args){
        AndroidManifestAnalyze a = new AndroidManifestAnalyze();
        a.xmlHandle("C:\\Users\\36202\\Desktop\\AndroidManifest.xml");
        output(a);
    }
}