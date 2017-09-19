import com.google.gson.Gson;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Kazumi Netease Lyrics Helper v0.3.0
 * <p>
 * �������������ַ������ӣ����ز������ʡ�
 * ====
 * yuki_ryoko@kotori.moe
 * github.com/yuki-ryoko/Kazumi-Netease-Lyrics-Helper
 */

public class GetNeteaseLrc {

    private static LinkedList<String> originalLrcList = new LinkedList<>();
    private static LinkedList<String> translationLrcList = new LinkedList<>();
    private static String finalLrc = "";

    public static void main(String[] args) {

        System.out.println("����������ӻ���� ID:");

        String shareLink = new Scanner(System.in).next();
        String songId = getSongId(shareLink);

        System.out.println("�ҵ����� ID: " + songId);

        // ��ȡ������ԭ���
        lrcProcessor(getOriginalLrc(songId, false), originalLrcList);

        // ��ȡ����������
        lrcProcessor(getOriginalLrc(songId, true), translationLrcList);

        // ���ԭ����뷭����
        if (translationLrcList.size() > 0) {

            for (int i = 0; i < originalLrcList.size(); i++) {

                boolean isFindTransLrc = false;

                for (int j = 0; j < translationLrcList.size(); j++) {

                    // �Ա�ʱ���
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

        // �������
        try {
            saveLrcFile(finalLrc);
        } catch (FileNotFoundException e) {
            System.out.println("����ļ�ʧ��: " + e);
        }


    }

    // ��ȡ���� ID
    private static String getSongId(String shareLink) {

        System.out.println("Ѱ�Ҹ��� ID...");

        // �Ƴ�����
        if (shareLink.contains("https")) {
            shareLink = shareLink.replace("https://music.163.com/#/m/song?id=", "");
        } else if (shareLink.contains("http")) {
            shareLink = shareLink.replace("http://music.163.com/#/m/song?id=", "");
        }

        // �Ƴ� userid
        try {
            shareLink = shareLink.replace(shareLink.substring(shareLink.indexOf("&userid"), shareLink.length()), "");
        } catch (Exception ignored) {
        }

        // �򵥼���Ƿ��Ǹ��� ID
        try {
            int temp = Integer.parseInt(shareLink);
        } catch (NumberFormatException e) {
            System.out.println("\n�������ӻ���� ID �������\n�ټ�");
            return "";
        }

        return shareLink;
    }

    // ��ȡԭ���
    private static String getOriginalLrc(String songId, boolean isTranslationLrc) {

        if (isTranslationLrc) {
            System.out.println("���ڻ�ȡ������...");
        } else {
            System.out.println("���ڻ�ȡԭ���...");
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
                System.out.println("��ȡ������ʧ��: " + localException.toString());
            } else {
                System.out.println("��ȡԭ���ʧ��: " + localException.toString());
            }
        }

        return temp;
    }

    // ������
    private static void lrcProcessor(String lrc, LinkedList<String> mLrcList) {

        String[] mLrc = null;

        // ����ԭ��ʷǸ������
        if (mLrcList == originalLrcList) {
            OriginalLrcBean originalLrcBean = new Gson().fromJson(lrc, OriginalLrcBean.class);
            if (null != originalLrcBean.lyric) {
                System.out.println("��ȡԭ��ʳɹ�");
                mLrc = originalLrcBean.lyric.split("\n");
            } else {
                System.out.println("�ƣ�û���");
                return;
            }
        }

        // �������ʷǸ������
        else {
            TranslationLrcBean translationLrcBean = new Gson().fromJson(lrc, TranslationLrcBean.class);
            if (null != translationLrcBean.tlyric.lyric) {
                System.out.println("��ȡ�����ʳɹ�");
                mLrc = translationLrcBean.tlyric.lyric.split("\n");
            } else {
                System.out.println("�ƣ�û������");
                return;
            }
        }

        // ����������
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

    // ���������ļ�
    private static void saveLrcFile(String data) throws FileNotFoundException {
        System.out.println("���ڱ����ļ�...");
        FileOutputStream fs = new FileOutputStream(new File("C:" + File.separator + ".lrc"));
        PrintStream p = new PrintStream(fs);
        p.println(data);
        p.close();
        System.out.println("����ļ��������: C:\\.lrc");
    }

    // ԭ��� JavaBean
    public class OriginalLrcBean {
        public String lyric;
    }

    // ������ JavaBean
    public class TranslationLrcBean {
        public Tlyric tlyric;

        public class Tlyric {
            public String lyric;
        }
    }


}
