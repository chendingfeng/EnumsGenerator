package com.dingfeng.execute;

import com.sun.tools.javac.api.JavacTool;
import javax.tools.JavaCompiler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description: 通过枚举java文件生成前端枚举
 * @author chendingfeng
 * @date 2021/9/10 11:53
 * @version 1.0
 */
public class EnumGenerator {

    /**
     * 编译指定文件夹中的所有java文件
     * @author chendingfeng
     * @date 2021/09/10 14:04
     * @param enumsPath 存放枚举Java文件路径
     * @return void 无返回值
     */
    public static void compilation(String enumsPath) {
        String packagePath = enumsPath.replaceAll("\\\\", "/");
        try {
            File[] list = new File(packagePath).listFiles();
            // 编译
            for (File file : list) {
                if (getFileExtension(file).equals("java")) {
                    String[] names = file.getName().split("\\.");
                    String className = Arrays.asList(names).get(0);
                    compilerFromJavaFile(enumsPath, className);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 转化成文本并写入js文件
     * @author chendingfeng
     * @date 2021/09/10 14:05
     * @param enumsPath 枚举java文件路径
     * @param outputPath 输出枚举JS文件路径
     * @return void 无返回值
     */
    public static void convertEnumJS(String enumsPath, String outputPath) {
        StringBuffer bufferObject = new StringBuffer();
        StringBuffer bufferArray = new StringBuffer();
        String packagePath = enumsPath.replaceAll("\\\\", "/");
        try {
            File[] list = new File(packagePath).listFiles();
            bufferObject.append("export class Enums {\n" +
                    "  // 构造函数\n" +
                    "  constructor() {\n");
            for (File file : list) {
                if (getFileExtension(file).equals("class")) {
                    String[] names = file.getName().split("\\.");
                    String className = Arrays.asList(names).get(0);
                    Class<Enum> enumsClass = classLoadForName(className, enumsPath);
                    // 需要生成的枚举类
                    toJson(enumsClass, bufferObject, bufferArray);
                }
            }
            StringBuffer buffer = bufferObject.append("\r\n").append(bufferArray);
            bufferObject.append("  }\n" +
                    "}\n" +
                    "\n" +
                    "\n" +
                    "class Enum {\n" +
                    "\n" +
                    "  /**\n" +
                    "   * 添加枚举字段\n" +
                    "   * field: 枚举字段\n" +
                    "   * label: 界面显示\n" +
                    "   * value: 枚举值\n" +
                    "   */\n" +
                    "  add (field, label, value, item = \"\") {\n" +
                    "    this[field] = {field, label, value, item }\n" +
                    "    return this\n" +
                    "  }\n" +
                    "\n" +
                    "  /**\n" +
                    "   * 根据枚举value获取其label\n" +
                    "   */\n" +
                    "  getLabelByValue (value) {\n" +
                    "    // 字段不存在返回‘’\n" +
                    "    if (value === undefined || value === null) {\n" +
                    "      return ''\n" +
                    "    }\n" +
                    "    for (let i in this) {\n" +
                    "      let e = this[i]\n" +
                    "      if (e && e.value === value) {\n" +
                    "        return e.label\n" +
                    "      }\n" +
                    "    }\n" +
                    "    return ''\n" +
                    "  }\n" +
                    "\n" +
                    "  /**\n" +
                    "   * 根据枚举label获取其value\n" +
                    "   */\n" +
                    "  getValueByLabel (label) {\n" +
                    "    // 字段不存在返回‘’\n" +
                    "    if (label === undefined || label === null) {\n" +
                    "      return ''\n" +
                    "    }\n" +
                    "    for (let i in this) {\n" +
                    "      let e = this[i]\n" +
                    "      if (e && e.label === label) {\n" +
                    "        return e.value\n" +
                    "      }\n" +
                    "    }\n" +
                    "    return ''\n" +
                    "  }\n" +
                    "\n" +
                    "  /**\n" +
                    "   * 根据枚举value获取其Item\n" +
                    "   */\n" +
                    "  getItemByValue (value) {\n" +
                    "    // 字段不存在返回‘’\n" +
                    "    if (value === undefined || value === null) {\n" +
                    "      return ''\n" +
                    "    }\n" +
                    "    for (let i in this) {\n" +
                    "      let e = this[i]\n" +
                    "      if (e && e.value === value) {\n" +
                    "        return e.item\n" +
                    "      }\n" +
                    "    }\n" +
                    "    return ''\n" +
                    "  }\n" +
                    "\n" +
                    "  /**\n" +
                    "   * 根据枚举value获取其Enum对象\n" +
                    "   */\n" +
                    "  getEnumByValue (value) {\n" +
                    "    // 字段不存在返回‘’\n" +
                    "    if (value === undefined || value === null) {\n" +
                    "      return ''\n" +
                    "    }\n" +
                    "    for (let i in this) {\n" +
                    "      let e = this[i]\n" +
                    "      if (e && e.value === value) {\n" +
                    "        return e\n" +
                    "      }\n" +
                    "    }\n" +
                    "    return ''\n" +
                    "  }\n" +
                    "\n" +
                    "}");
            writeJs(buffer, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 通过类名反射加载class对象
     * @author chendingfeng
     * @date 2021/09/10 14:11
     * @param enumsPath
     * @param fileName
     * @return java.lang.Class
     */
    public static Class classLoadForName(String fileName, String enumsPath)  {
        Class<?> clazz = null;
        try {
            MyClassLoader loader = new MyClassLoader(enumsPath);//使用自定义ClassLoader
            clazz = loader.findClass(fileName);
            System.err.println("正在生成枚举对象: "+fileName);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return clazz;
    }

    /**
     * 编译指定文件夹中的java文件
     * @author chendingfeng
     * @date 2021/09/10 14:07
     * @param fileName java枚举文件名
     * @param dirPath 枚举java文件路径
     * @return java.lang.Class
     */
    public static void compilerFromJavaFile(String dirPath,  String fileName) {
        String dir = dirPath.replaceAll("\\.","/").replaceAll("\\\\", "/");
        String filePath = dir.concat( "/" ).concat(fileName).concat(".java");

        //编译
        JavaCompiler javac = JavacTool.create();
        System.err.println("正在编译java文件: "+filePath);
        int compilationResult = javac.run(null, null, null, filePath);

        if  (compilationResult != 0)//compilationResult == 0,说明编译成功，在Java文件的同目录下会生成相应的class文件
        {
            throw new IllegalArgumentException("编译失败");
        }
    }

    /**
     * 写文件操作
     * @author chendingfeng
     * @date 2021/09/10 14:15
     * @param stringBuffer
     * @param outputPath 枚举的js文件输出路径
     * @return void
     */
    public static void writeJs(StringBuffer stringBuffer, String outputPath) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputPath);
            OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
            osw.write(stringBuffer.toString());
            osw.close();
            System.out.println("已帮你将枚举写入JS文件：" + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 驼峰转大写下划线，并去掉_ENUM
     * @author chendingfeng
     * @date 2021/09/10 14:16
     * @param str
     * @return java.lang.String
     */
    public static String toUnderline(String str) {
        String result = underline(str).toString();
        return result.substring(1, result.length()).toUpperCase().replace("_ENUM", "");
    }

    /**
     * 驼峰转下划线，第一位是下划线
     * @author chendingfeng
     * @date 2021/09/10 14:16
     * @param str
     * @return java.lang.StringBuffer
     */
    private static StringBuffer underline(String str) {
        Pattern pattern = Pattern.compile("[A-Z]");
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer(str);
        if(matcher.find()) {
            sb = new StringBuffer();
            matcher.appendReplacement(sb,"_"+matcher.group(0).toLowerCase());
            matcher.appendTail(sb);
        }else {
            return sb;
        }
        return underline(sb.toString());
    }

    /**
     * 获取文件名后缀
     * @author chendingfeng
     * @date 2021/09/10 14:16
     * @param file
     * @return java.lang.String
     */
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".")+1);
        } else {
            return "";
        }
    }

    /**
     * 自定义ClassLoader
     * @author chendingfeng
     * @date 2021/09/10 14:14
     * @return
     */
    private static final class MyClassLoader extends ClassLoader {

        private String classDir;
        @Override
        public Class<?> findClass(String name) {
            String realPath = classDir + name.replace(".","/").replace("\\\\", "/") + ".class";//class文件的真实路径
            byte[] cLassBytes = null;
            Path path = null;

            try {
                path = Paths.get(new URI(realPath));
                cLassBytes = Files.readAllBytes(path);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
            Class clazz = defineClass(null, cLassBytes, 0, cLassBytes.length);//调用父类的defineClass方法
            return clazz;
        }

        public MyClassLoader(String classDir) {
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Windows")) {
                this.classDir = "file:/".concat(classDir).concat("/").replaceAll("\\\\", "/");
            } else {
                this.classDir = "file:".concat(classDir).concat("/").replaceAll("\\\\", "/");
            }
        }

    }

    private static void toJson(Class clazz, StringBuffer bufferObject, StringBuffer bufferArray) throws Exception {
        String key = toUnderline(clazz.getSimpleName());
        toJson(clazz, key, bufferObject, bufferArray);
    }

    private static void toJson(Class clazz, String key, StringBuffer bufferObject, StringBuffer bufferArray) throws Exception {
        Object[] objects = clazz.getEnumConstants();
        Method name = clazz.getMethod("name");
        Method getDesc = clazz.getMethod("getDescription");
        Method getValue = clazz.getMethod("getValue");

        // 生成对象
        bufferObject.append("this.");
        bufferObject.append(key).append(" = new Enum()");
        bufferObject.append("\r\n");
        for (int i = 0; i < objects.length; i++) {
            Object obj = objects[i];
            bufferObject.append(".add('"+name.invoke(obj)+"', ").append("'"+getDesc.invoke(obj)+"', ").append(getValue.invoke(obj)).append(")");
            bufferObject.append("\r\n");
        }
        bufferObject.append("\r\n");
    }

}
