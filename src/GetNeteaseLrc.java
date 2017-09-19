import com.google.gson.Gson;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Kazumi Netease Lyrics Helper v0.3.0
 * <p>
 * 根据网易云音乐分享链接，下载并保存歌词。
 * ====
 * yuki_ryoko@kotori.moe
 * github.com/yuki-ryoko/Kazumi-Netease-Lyrics-Helper
 */

public class GetNeteaseLrc {

    private static LinkedList<String> originalLrcList = new LinkedList<>();
    private static LinkedList<String> translationLrcList = new LinkedList<>();
    private static String finalLrc = "";

    public static void main(String[] args) {

        System.out.println("输入分享链接或歌曲 ID:");

        String shareLink = new Scanner(System.in).next();
        String songId = getSongId(shareLink);

        System.out.println("找到歌曲 ID: " + songId);

        // 获取并处理原歌词
        lrcProcessor(getOriginalLrc(songId, false), originalLrcList);

        // 获取并处理翻译歌词
        lrcProcessor(getOriginalLrc(songId, true), translationLrcList);

        // 组合原歌词与翻译歌词
        if (translationLrcList.size() > 0) {

            for (int i = 0; i < originalLrcList.size(); i++) {

                boolean isFindTransLrc = false;

                for (int j = 0; j < translationLrcList.size(); j++) {

                    // 对比时间戳
                    String originalLrcTimestamp = originalLrcList.get(i).substring(0, originalLrcList.get(i).indexOf("]") + 1);
                    String translationLrcTimestamp = translationLrcList.get(j).substring(0, translationLrcList.get(j).indexOf("]") + 1);

                    if (originalLrcTimestamp.equals(translationLrcTimestamp)) {
                        finalLrc += (originalLrcList.get(i) + " / " + translationLrcList.get(j).replace(translationLrcTimestamp, "") + "\r\n");
                        isFindTransLrc = true;
                    }
                }

                if (!isFindTransLrc) {
                    finalLrc += originalLrcList.get(i) + "\r\n";
                }
            }
        }

        else {
            for (int i = 0; i < originalLrcList.size(); i++) {
                finalLrc += originalLrcList.get(i) + "\r\n";
            }
        }

        // 最终输出
        try {
            saveLrcFile(finalLrc);
        } catch (FileNotFoundException e) {
            System.out.println("输出文件失败: " + e);
        }


    }

    // 获取歌曲 ID
    private static String getSongId(String shareLink) {

        System.out.println("寻找歌曲 ID...");

        // 移除域名
        if (shareLink.contains("https")) {
            shareLink = shareLink.replace("https://music.163.com/#/m/song?id=", "");
        } else if (shareLink.contains("http")) {
            shareLink = shareLink.replace("http://music.163.com/#/m/song?id=", "");
        }

        // 移除 userid
        try {
            shareLink = shareLink.replace(shareLink.substring(shareLink.indexOf("&userid"), shareLink.length()), "");
        } catch (Exception ignored) {
        }

        // 简单检测是否是歌曲 ID
        try {
            int temp = Integer.parseInt(shareLink);
        } catch (NumberFormatException e) {
            System.out.println("\n分享链接或歌曲 ID 输入错误\n再见");
            return "";
        }

        return shareLink;
    }

    // 获取原歌词
    private static String getOriginalLrc(String songId, boolean isTranslationLrc) {

        if (isTranslationLrc) {
            System.out.println("正在获取翻译歌词...");
        } else {
            System.out.println("正在获取原歌词...");
        }

        String temp = null;

        try {
            URL localURL = null;

            if (isTranslationLrc) {
                localURL = new URL("https://music.163.com/api/song/lyric?os=pc&id=" + songId + "&tv=-1");
            } else {
                localURL = new URL("http://music.163.com/api/song/media?id=" + songId);
            }

            InputStream localInputStream = localURL.openStream();
            InputStreamReader localInputStreamReader = new InputStreamReader(localInputStream, "utf-8");
            BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader);

            String str = null;
            while ((str = localBufferedReader.readLine()) != null) {
                temp = str;
            }
            localBufferedReader.close();
            localInputStreamReader.close();
            localInputStream.close();
        } catch (Exception localException) {
            if (isTranslationLrc) {
                System.out.println("获取翻译歌词失败: " + localException.toString());
            } else {
                System.out.println("获取原歌词失败: " + localException.toString());
            }
        }

        return temp;
    }

    // 处理歌词
    private static void lrcProcessor(String lrc, LinkedList<String> mLrcList) {

        String[] mLrc = null;

        // 处理原歌词非歌词数据
        if (mLrcList == originalLrcList) {
            OriginalLrcBean originalLrcBean = new Gson().fromJson(lrc, OriginalLrcBean.class);
            if (null != originalLrcBean.lyric) {
                System.out.println("获取原歌词成功");
                mLrc = originalLrcBean.lyric.split("\n");
            } else {
                System.out.println("淦，没歌词");
                return;
            }
        }

        // 处理翻译歌词非歌词数据
        else {
            TranslationLrcBean translationLrcBean = new Gson().fromJson(lrc, TranslationLrcBean.class);
            if (null != translationLrcBean.tlyric.lyric) {
                System.out.println("获取翻译歌词成功");
                mLrc = translationLrcBean.tlyric.lyric.split("\n");
            } else {
                System.out.println("淦，没翻译歌词");
                return;
            }
        }

        // 处理歌词数据
        for (int i = 0; i < mLrc.length; i++) {

            if (mLrc[i].indexOf("][") >= 0) {
                String[] temp = mLrc[i].split("]");
                if (!mLrc[i].substring(mLrc[i].length() - 1, mLrc[i].length()).equals("]")) {
                    for (int j = 0; j < temp.length - 1; j++) {
                        mLrcList.add(temp[j] + "]" + temp[temp.length - 1]);
                    }
                } else {
                    for (int j = 0; j < temp.length - 1; j++) {
                        mLrcList.add(temp[j] + "] ");
                    }
                }
            } else {
                mLrcList.add(mLrc[i]);
            }
        }
    }

    // 保存最终文件
    private static void saveLrcFile(String data) throws FileNotFoundException {
        System.out.println("正在保存文件...");
        FileOutputStream fs = new FileOutputStream(new File("C:" + File.separator + ".lrc"));
        PrintStream p = new PrintStream(fs);
        p.println(data);
        p.close();
        System.out.println("歌词文件已输出到: C:\\.lrc");
    }

    // 原歌词 JavaBean
    public class OriginalLrcBean {
        public String lyric;
    }

    // 翻译歌词 JavaBean
    public class TranslationLrcBean {
        public Tlyric tlyric;

        public class Tlyric {
            public String lyric;
        }
    }


}
