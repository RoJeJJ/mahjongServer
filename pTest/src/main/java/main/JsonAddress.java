/**
  * Copyright 2017 bejson.com 
  */
package main;
import java.util.List;

/**
 * Auto-generated: 2017-12-02 14:35:52
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class JsonAddress {

    private List<Double> queryLocation;
    private List<AddrInfo> addrList;
    public void setQueryLocation(List<Double> queryLocation) {
         this.queryLocation = queryLocation;
     }
     public List<Double> getQueryLocation() {
         return queryLocation;
     }

    public void setAddrList(List<AddrInfo> addrInfo) {
         this.addrList = addrInfo;
     }
     public List<AddrInfo> getAddrList() {
         return addrList;
     }

}