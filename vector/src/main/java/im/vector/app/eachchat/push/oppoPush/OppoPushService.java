package im.vector.app.eachchat.push.oppoPush;

import android.content.Context;

import com.facebook.stetho.common.LogUtil;
import com.heytap.msp.push.mode.DataMessage;
import com.heytap.msp.push.service.CompatibleDataMessageCallbackService;
import com.heytap.msp.push.service.DataMessageCallbackService;

/**
 * Created by zhouguanjie on 2020/1/17.
 */
public class OppoPushService extends CompatibleDataMessageCallbackService {

    @Override
    public void processMessage(Context context, DataMessage dataMessage) {
        super.processMessage(context, dataMessage);
        try {
            LogUtil.i("## oppo processMessage dataMessage = " + dataMessage.toString());
//            PushHelper.getInstance().syncMessage(context);
//            PushHelper.getInstance().setBadge(context);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}

