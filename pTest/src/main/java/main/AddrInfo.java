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
public class AddrInfo {

    private String type;
    private int status;
    private String name;
    private String id;
    private String admCode;
    private String admName;
    private String addr;
    private List<Double> nearestPoint;
    private double distance;
    public void setType(String type) {
         this.type = type;
     }
     public String getType() {
         return type;
     }

    public void setStatus(int status) {
         this.status = status;
     }
     public int getStatus() {
         return status;
     }

    public void setName(String name) {
         this.name = name;
     }
     public String getName() {
         return name;
     }

    public void setId(String id) {
         this.id = id;
     }
     public String getId() {
         return id;
     }

    public void setAdmCode(String admCode) {
         this.admCode = admCode;
     }
     public String getAdmCode() {
         return admCode;
     }

    public void setAdmName(String admName) {
         this.admName = admName;
     }
     public String getAdmName() {
         return admName;
     }

    public void setAddr(String addr) {
         this.addr = addr;
     }
     public String getAddr() {
         return addr;
     }

    public void setNearestPoint(List<Double> nearestPoint) {
         this.nearestPoint = nearestPoint;
     }
     public List<Double> getNearestPoint() {
         return nearestPoint;
     }

    public void setDistance(double distance) {
         this.distance = distance;
     }
     public double getDistance() {
         return distance;
     }

}