package local.hal.st21.android.karaokemap;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class StoreSearchMapsAnimation extends Animation{
    final int addHeight;
    View view;
    int startHeight;

    /**
     * コンストラクタ
     *
     * @param view アニメーションさせたいView
     * @param addHeight アニメーション後に追加する高さ
     * @param startHeight アニメーション開始時の高さ
     */
    public StoreSearchMapsAnimation(View view, int addHeight, int startHeight) {
        this.view = view;
        this.addHeight = addHeight;
        this.startHeight = startHeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newHeight = (int) (startHeight + addHeight * interpolatedTime);
        view.getLayoutParams().height = newHeight;
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
