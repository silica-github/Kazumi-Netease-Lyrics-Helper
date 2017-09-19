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

		System.out.println("输入分享地址：");

		// 获取输入的分享地址
		getInputUrl();
	}

	// 获取输入的分享地址
	public static void getInputUrl() {

		Scanner localScanner = new Scanner(System.in);

		// 获取输入 url
		getSongId(localScanner.next());
	}

	// 获取连接中的 id
	public static void getSongId(String url) {

		String songId = null;

		try {
			songId = url.substring(url.indexOf("id=") + 3, url.indexOf("&userid"));
		} catch (Exception localException) {
			System.out.println("请输入完整的分享链接\n再见");
			return;
		}

		// 获取原歌词
		getLrc(songId);

		// 获取中文歌词
		getChineseLrc(songId);

		System.out.println("=============================");
		System.out.println("原歌词长度: " + mLrcData.size());
		System.out.println("中文歌词长度: " + mChineseLrcData.size());
		System.out.println("=============================");

		// for (int i = 0; i < mLrcData.size(); i++) {
		// try {
		// finalLrc += mLrcData.get(i) + " / " + mChineseLrcData.get(i) +
		// "\r\n";
		// } catch (Exception e) {
		// finalLrc += mLrcData.get(i) + "\r\n";
		// }
		// }

		// 没有中文歌词 (翻译歌词)
		if (null == mChineseLrcData || 0 >= mChineseLrcData.size()) {
			for (int i = 0; i < mLrcData.size(); i++) {
				finalLrc += mLrcData.get(i) + "\r\n";
			}
		} else {
			for (int i = 0; i < mLrcData.size(); i++) {

				boolean isFind = false;

				for (int j = 0; j < mChineseLrcData.size(); j++) {

					// 对比时间戳
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
			System.out.println("输出文件失败: " + e);
		}
	}

	public static String finalLrc = "";

	// 获取原歌词
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

	// 处理非歌词数据
	public static void cutHead(String paramString) throws IOException {
		GetNeteaseLrc.LrcItem localLrcItem = (GetNeteaseLrc.LrcItem) new Gson().fromJson(paramString,
				GetNeteaseLrc.LrcItem.class);
		if (null != localLrcItem.lyric) {
			readString(localLrcItem.lyric, mLrcData);
		} else {
			System.out.println("淦，没歌词");
			return;
		}
	}

	// 处理歌词数据并加入数组
	public static String readString(String pathname, LinkedList<String> mData) throws IOException {
		BufferedReader reader = new BufferedReader(new StringReader(pathname));
		StringBuilder sb = new StringBuilder();

		String line = "";
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\r\n");

			// 分隔时间戳与正文内容
			String[] temp = line.split("]");

			// 处理空字符串时间戳
			if (1 == temp.length && !line.trim().equals("")) {
				temp = new String[] { temp[0], " " };
			}

			// 将处理完的正文加入到数组
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
			System.out.println("淦，没中文歌词");
		}
	}

	public static class ChineseLrcItem {
		public Tlyric tlyric;

		public class Tlyric {
			public String lyric;
		}
	}

	// 保存最终文件
	public static void saveLrcFile(String data) throws FileNotFoundException {
		FileOutputStream fs = new FileOutputStream(new File("C:" + File.separator + ".lrc"));
		PrintStream p = new PrintStream(fs);
		p.println(data);
		p.close();
		System.out.println("歌词文件已输出到: C:\\.lrc");
	}
}
