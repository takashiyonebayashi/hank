package com.rapleaf.hank.client;

import com.rapleaf.hank.generated.HankBulkResponse;
import com.rapleaf.hank.generated.HankResponse;
import com.rapleaf.hank.generated.SmartClient;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.util.List;

public interface HankSmartClientIface extends SmartClient.Iface {

  public HankResponse get(String domain_name, ByteBuffer key);

  public HankBulkResponse getBulk(String domain_name, List<ByteBuffer> keys);

  public FutureGet concurrentGet(String domainName, ByteBuffer key) throws TException;

  public List<FutureGet> concurrentGet(String domainName, List<ByteBuffer> key) throws TException;

  public abstract void stop();

}
