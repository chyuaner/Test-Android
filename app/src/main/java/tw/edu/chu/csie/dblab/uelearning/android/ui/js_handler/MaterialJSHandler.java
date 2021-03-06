package tw.edu.chu.csie.dblab.uelearning.android.ui.js_handler;

import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.util.Date;

import tw.edu.chu.csie.dblab.uelearning.android.database.DBProvider;
import tw.edu.chu.csie.dblab.uelearning.android.ui.MaterialActivity;
import tw.edu.chu.csie.dblab.uelearning.android.util.LogUtils;
import tw.edu.chu.csie.dblab.uelearning.android.util.TimeUtils;

/**
 * Created by yuan on 2015/1/24.
 */
public class MaterialJSHandler {

    protected int studyActivityId;
    protected int targetId;
    private MaterialActivity context;

    public MaterialJSHandler(MaterialActivity context, int targetId) {
        this.context = context;
        this.targetId = targetId;
        DBProvider db = new DBProvider(context);
        this.studyActivityId = db.get_activity_id();
    }

    @JavascriptInterface
    public void pressFinishButton() {
        LogUtils.Insert.material_pressFinishButton(context, studyActivityId, targetId);
    }

    @JavascriptInterface
    public void answerCorrect(int topicId, String atIndex) {
        LogUtils.Insert.material_answer(context, studyActivityId, targetId, topicId, atIndex, true);
    }

    @JavascriptInterface
    public void answerError(int topicId, String atIndex) {
        DBProvider db = new DBProvider(context);
        int saId = db.get_activity_id();
        LogUtils.Insert.material_answer(context, saId, targetId, topicId, atIndex, true);
    }

    /**
     * 結束學習
     *
     * 當接到由教材網頁發出已完成學習的訊號時，觸發MaterialActivity的學習完成函式
     */
    @JavascriptInterface
    public void learnFinish() {
        context.finishLearn();
    }

    /**
     * 相容於一代教材的結束學習
     *
     * 注意！這僅是為了相容一代教材因應的函式，請避免使用此方法。回答問題的傳遞請另外使用。
     * 當接到由教材網頁發出已完成學習的訊號時，接應學生作答狀況，並觸發MaterialActivity的學習完成函式
     * @param ansQID 問題編號陣列
     * @param ansCheck 回答內容陣列
     */
    @JavascriptInterface
    public void learnFinish(String[] ansQID, String[] ansCheck) {
        learnFinish();
    }

    @JavascriptInterface
    public void goBack() {
        LogUtils.Insert.material_goBack(context, studyActivityId, targetId);
    }
}
