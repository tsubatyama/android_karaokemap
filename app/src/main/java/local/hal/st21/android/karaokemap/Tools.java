package local.hal.st21.android.karaokemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Tools {
    public static String is2String(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuffer sb = new StringBuffer();
        char[] b = new char[1024];
        int line;
        while (0 <= (line = reader.read(b))) {
            sb.append(b, 0, line);
        }
        return sb.toString();
    }


    /**
     * 数値をカンマ区切りの数値にし、Stringで返す
     * @param num
     * @return
     */
    public static String intToStringCom(int num) {
        String StrNum = String.valueOf(num);
        StringBuffer sb = new StringBuffer(StrNum);
        StrNum = sb.reverse().toString();
        String CommNum ="";
        for(int i = 1; StrNum.length()>= i ; i++) {
            CommNum += StrNum.charAt(i-1);

            if(i%3 ==0 && !(StrNum.length() < i+1)) {
                CommNum +=",";
            }
        }
        sb= new StringBuffer(CommNum);
        CommNum = sb.reverse().toString();
        return CommNum;
    }


    /**
     * 数字をカンマ区切りの数値にし、Stringで返す
     * @param num
     * @return
     */
    public static String StrNumToStringCom(String num) {
        StringBuffer sb = new StringBuffer(num);
        String StrNum = sb.reverse().toString();
        String CommNum ="";
        for(int i = 1; StrNum.length()>= i ; i++) {
            CommNum += StrNum.charAt(i-1);

            if(i%3 ==0 &&!(StrNum.length() < i+1 )) {
                CommNum +=",";
            }
        }
        sb= new StringBuffer(CommNum);
        CommNum = sb.reverse().toString();
        return CommNum;
    }

    /**
     * カンマ区切りされた数値をカンマなしにStringで返す
     * @param ComNum
     * @return
     */
    public static String StringComToString(String ComNum) {
        String StrNum = "";
        for(int i = 0; ComNum.length() > i ; i++) {
            if(!",".equals(String.valueOf(ComNum.charAt(i)))) {
                StrNum += ComNum.charAt(i);
            }
        }

        return StrNum;

    }

}
