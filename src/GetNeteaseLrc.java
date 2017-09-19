import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.Scanner;

import com.google.gson.Gson;

/**
 * Kazumi Netease Lyrics Helper v0.3.0
 * 
 * yuki_ryoko@kotori.moe
 */

public class GetNeteaseLrc {

	public static LinkedList<String> mLrcData = new LinkedList<>();
	public static LinkedList<String> mChineseLrcData = new LinkedList<>();

	public static void main(String[] paramArrayOfString) {

		System.out.println("��������ַ��");

		// ��ȡ����ķ����ַ
		getInputUrl();
	}

	// ��ȡ����ķ����ַ
	public static void getInputUrl() {

		Scanner localScanner = new Scanner(System.in);

		// ��ȡ���� url
		getSongId(localScanner.next());
	}

	// ��ȡ�����е� id
	public static void getSongId(String url) {

		String songId = null;

		try {
			songId = url.substring(url.indexOf("id=") + 3, url.indexOf("&userid"));
		} catch (Exception localException) {
			System.out.println("�����������ķ�������\n�ټ�");
			return;
		}

		// ��ȡԭ���
		getLrc(songId);

		// ��ȡ���ĸ��
		getChineseLrc(songId);

		System.out.println("=============================");
		System.out.println("ԭ��ʳ���: " + mLrcData.size());
		System.out.println("���ĸ�ʳ���: " + mChineseLrcData.size());
		System.out.println("=============================");

		// for (int i = 0; i < mLrcData.size(); i++) {
		// try {
		// finalLrc += mLrcData.get(i) + " / " + mChineseLrcData.get(i) +
		// "\r\n";
		// } catch (Exception e) {
		// finalLrc += mLrcData.get(i) + "\r\n";
		// }
		// }

		// û�����ĸ�� (������)
		if (null == mChineseLrcData || 0 >= mChineseLrcData.size()) {
			for (int i = 0; i < mLrcData.size(); i++) {
				finalLrc += mLrcData.get(i) + "\r\n";
			}
		} else {
			for (int i = 0; i < mLrcData.size(); i++) {

				boolean isFind = false;

				for (int j = 0; j < mChineseLrcData.size(); j++) {

					// �Ա�ʱ���
					if (mLrcData.get(i).substring(0, mLrcData.get(i).indexOf("]") + 1)
							.equals(mChineseLrcData.get(j).substring(0, mLrcData.get(j).indexOf("]") + 1))
							&& !mLrcData.get(i).trim().equals("")) {

						System.out.print(mLrcData.get(i) + " / "
								+ mChineseLrcData.get(j).replace(
										mChineseLrcData.get(j).substring(0, mChineseLrcData.get(j).indexOf("]") + 1),
										"")
								+ "\r\n");

						isFind = true;
					}
				}

				if (!isFind) {
					System.out.print(mLrcData.get(i) + "\r\n");
				}
			}
		}

		try {
			saveLrcFile(finalLrc);
		} catch (FileNotFoundException e) {
			System.out.println("����ļ�ʧ��: " + e);
		}
	}

	public static String finalLrc = "";

	// ��ȡԭ���
	public static void getLrc(String paramString) {
		try {
			URL localURL = new URL("http://music.163.com/api/song/media?id=" + paramString);
			InputStream localInputStream = localURL.openStream();
			InputStreamReader localInputStreamReader = new InputStreamReader(localInputStream, "utf-8");
			BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader);

			String str = null;
			while ((str = localBufferedReader.readLine()) != null) {
				cutHead(str);
			}
			localBufferedReader.close();
			localInputStreamReader.close();
			localInputStream.close();
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	// ����Ǹ������
	public static void cutHead(String paramString) throws IOException {
		GetNeteaseLrc.LrcItem localLrcItem = (GetNeteaseLrc.LrcItem) new Gson().fromJson(paramString,
				GetNeteaseLrc.LrcItem.class);
		if (null != localLrcItem.lyric) {
			readString(localLrcItem.lyric, mLrcData);
		} else {
			System.out.println("�ƣ�û���");
			return;
		}
	}

	// ���������ݲ���������
	public static String readString(String pathname, LinkedList<String> mData) throws IOException {
		BufferedReader reader = new BufferedReader(new StringReader(pathname));
		StringBuilder sb = new StringBuilder();

		String line = "";
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\r\n");

			// �ָ�ʱ�������������
			String[] temp = line.split("]");

			// ������ַ���ʱ���
			if (1 == temp.length && !line.trim().equals("")) {
				temp = new String[] { temp[0], " " };
			}

			// ������������ļ��뵽����
			for (int j = 0; j < temp.length - 1; j++) {

				if (temp[j].length() >= 0) {
					mData.add(temp[j] + "]" + temp[temp.length - 1]);
				}

			}

			// mData.add(line);
		}
		reader.close();
		return sb.toString();
	}

	public static class LrcItem {
		public String lyric;
	}

	public static void getChineseLrc(String paramString) {
		try {
			URL localURL = new URL("https://music.163.com/api/song/lyric?os=pc&id=" + paramString + "&tv=-1");
			InputStream localInputStream = localURL.openStream();
			InputStreamReader localInputStreamReader = new InputStreamReader(localInputStream, "utf-8");
			BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader);

			String str = null;
			while ((str = localBufferedReader.readLine()) != null) {
				cutChineseHead(str);
			}
			localBufferedReader.close();
			localInputStreamReader.close();
			localInputStream.close();
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public static void cutChineseHead(String paramString) throws IOException {
		GetNeteaseLrc.ChineseLrcItem localLrcItem = (GetNeteaseLrc.ChineseLrcItem) new Gson().fromJson(paramString,
				GetNeteaseLrc.ChineseLrcItem.class);
		if (null != localLrcItem.tlyric.lyric) {
			readString(localLrcItem.tlyric.lyric, mChineseLrcData);
		} else {
			System.out.println("�ƣ�û���ĸ��");
		}
	}

	public static class ChineseLrcItem {
		public Tlyric tlyric;

		public class Tlyric {
			public String lyric;
		}
	}

	// ���������ļ�
	public static void saveLrcFile(String data) throws FileNotFoundException {
		FileOutputStream fs = new FileOutputStream(new File("C:" + File.separator + ".lrc"));
		PrintStream p = new PrintStream(fs);
		p.println(data);
		p.close();
		System.out.println("����ļ��������: C:\\.lrc");
	}
}
