package util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Arrays;

/**
 * @Author: Waterless
 * @Date: 2022/07/06/11:34
 * @Description: 拼音的工具类
 * 一般来说项目都是从工具类或者数据库相关的操作开始写起
 * 将汉语拼音的字符映射为字母字符串
 */
public class PinyinUtil {
    //定义汉语拼音的配置 全局常量 必须在定义时初始化
    private static final HanyuPinyinOutputFormat FORMAT;
    //所有的中文对应的unicode编码区间
    private static final String CHINESE_PATTERN = "[\\u4E00-\\u9FA5]";
    //代码块就是在进行一些项目配置的初始化操作
    static {
        //使用静态代码块初始化 类一加载执行，还可以进行配置相关的工作
        FORMAT = new HanyuPinyinOutputFormat();
        //设置转换后的英文字母为全小写
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //设置转换后的英文字母是否带音调 tone(音调)
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE); //不带音调
        //特殊拼音用v替代 u: -> v
        FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V); //带v
    }

    //传入任意的文件名称，就能将该文件名称转为字母字符串和首字母小写字符串
    //eg: 张三 -> zhangsan 和 zs
    //若文件中还包含其他的字符 如英文数字等，不需要处理，直接保存
    //渊酱fuchi123 -> yuanjiangfuchi123 \ yjfuchi123
    public static String[] getPinyinByFileName(String fileName) {
        //第一个字符串为文件名全拼
        //第二个字符串为文件名首字母
        String[] ret = new String[2];
        //核心操作就是遍历文件中的每个字符,碰到中文就处理 非中文就保留
        StringBuilder allNameAppender = new StringBuilder();
        StringBuilder firstCaseAppender = new StringBuilder();
        //fileName = 张三c真帅
        // c = 张
        for (char c : fileName.toCharArray()) {
            //不考虑多音字，就使用第一个返回值作为我们的参数
            try {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c,FORMAT);
                if (pinyins == null || pinyins.length == 0) {
                    //碰到非中文字符，直接保留
                    allNameAppender.append(c);
                    firstCaseAppender.append(c);
                } else {
                    //碰到中文字符，取第一个多音字的返回值 和 -> [he,hu,huo...]
                    allNameAppender.append(pinyins[0]);
                    // he -> h
                    firstCaseAppender.append(pinyins[0].charAt(0));
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                //非中文字符直接保留
                allNameAppender.append(c);
                firstCaseAppender.append(c);
            }
        }
        ret[0] = allNameAppender.toString();
        ret[1] = firstCaseAppender.toString();
        return ret;
    }

    public static void main(String[] args) throws BadHanyuPinyinOutputFormatCombination {
//        char c = '和';
//        char c = '绿';
//        //任意一个汉语的字符转换为字母字符串，得到的都是一个字符串数组，存在多音字
//        String[] ret = PinyinHelper.toHanyuPinyinStringArray(c,FORMAT);
//        System.out.println(Arrays.toString(ret));
        String str1 = "妙妙真好";
        System.out.println(Arrays.toString(getPinyinByFileName(str1)));
        String str2 = "石思妙14s0123";
        System.out.println(Arrays.toString(getPinyinByFileName(str2)));
    }

    //判断给定的字符串是否包含中文
    //Java中 字符采用Unicode编码 所有的中文都对应一个不同的unicode编码值
    //只需要知道所有中文的编码起止区间即可
    public static boolean containsChinese(String str) {
        // .* 就表示 0 到 n 个字符
        return str.matches(".*" + CHINESE_PATTERN + ".*");
    }
}
