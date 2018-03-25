package site.iway.androidhelpers;

@Deprecated
public interface RPCListener {

    public void onRequestOK(RPCInfo rpcInfo, Object data);

    public void onRequestER(RPCInfo rpcInfo, Exception e);

}
