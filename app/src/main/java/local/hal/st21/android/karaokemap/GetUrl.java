package local.hal.st21.android.karaokemap;

/*
サーバーと連携するときのURLを取得するクラス
fixedUrlの後にサーブレット名を追加し作成。
 */
public class GetUrl {
    private static final String fixedUrl = "http://10.0.2.2:8080/karaoke_map/";//karaoke_map

    public static final String storeMapUrl = fixedUrl + "MapServlet";
    public static final String ReInsertUrl = fixedUrl + "ReInsertServlet";
    public static final String photoUrl = fixedUrl + "ImageDisplay.jsp";
}

