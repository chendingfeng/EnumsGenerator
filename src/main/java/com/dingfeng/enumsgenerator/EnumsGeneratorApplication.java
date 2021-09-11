package com.dingfeng.enumsgenerator;

import com.dingfeng.execute.EnumGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class EnumsGeneratorApplication {

    /**
     * 主函数
     * @author chendingfeng
     * @date 2021/09/10 14:19
     * @param args 第一参数：存放java枚举文件的目录；第二参数：输出枚举JS文件位置
     * @return void
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("请输入对应参数！");
            System.out.println("第一参数：存放java枚举文件的目录；第二参数：输出枚举JS文件位置");
                Scanner inputParam = new Scanner(System.in);
                System.out.println("请输入存放java枚举文件的目录：");
                String enumsPath = inputParam.next();
                System.out.println("请输入需输出枚举JS文件的目录：");
                String outputPath = inputParam.next();
                System.out.println("再次回车执行枚举转化!");
                inputParam.nextLine();
                inputParam.hasNextLine();
                EnumGenerator.compilation(enumsPath);
                EnumGenerator.convertEnumJS(enumsPath, outputPath);

        } else {
            EnumGenerator.compilation(args[0]);
            EnumGenerator.convertEnumJS(args[0], args[1]);
        }

    }

}
