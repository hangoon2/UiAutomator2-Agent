

package com.onycom.uiautomator2.handler;

import com.onycom.uiautomator2.handler.request.SafeRequestHandler;
import com.onycom.uiautomator2.common.exceptions.InvalidArgumentException;
import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;
import com.onycom.uiautomator2.model.NetworkConnectionEnum;
import com.onycom.uiautomator2.model.api.NetworkConnectionModel;
import com.onycom.uiautomator2.utils.WifiHandler;

import static com.onycom.uiautomator2.utils.ModelUtils.toModel;

public class NetworkConnection extends SafeRequestHandler {

    public NetworkConnection(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) {
        int requestedType = toModel(request, NetworkConnectionModel.class).type;
        NetworkConnectionEnum networkType = NetworkConnectionEnum.getNetwork(requestedType);
        switch (networkType) {
            case WIFI:
                return WifiHandler.toggle(true, getSessionId(request));
            case DATA:
            case AIRPLANE:
            case ALL:
            case NONE:
                throw new RuntimeException(String.format("Setting Network Connection to '%s' is not implemented",
                        networkType.getNetworkType()));
            default:
                throw new InvalidArgumentException("Invalid Network Connection type: " + requestedType);
        }
    }
}
