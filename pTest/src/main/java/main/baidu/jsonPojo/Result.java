/**
  * Copyright 2017 bejson.com 
  */
package main.baidu.jsonPojo;
import java.util.List;

/**
 * Auto-generated: 2017-12-02 15:6:14
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Result {

    private Location location;
    private String formatted_address;
    private String business;
    private AddressComponent addressComponent;
    private List<Pois> pois;
    private List<String> roads;
    private List<String> poiRegions;
    private String sematic_description;
    private int cityCode;
    public void setLocation(Location location) {
         this.location = location;
     }
     public Location getLocation() {
         return location;
     }

    public void setFormatted_address(String formatted_address) {
         this.formatted_address = formatted_address;
     }
     public String getFormatted_address() {
         return formatted_address;
     }

    public void setBusiness(String business) {
         this.business = business;
     }
     public String getBusiness() {
         return business;
     }

    public void setAddressComponent(AddressComponent addressComponent) {
         this.addressComponent = addressComponent;
     }
     public AddressComponent getAddressComponent() {
         return addressComponent;
     }

    public void setPois(List<Pois> pois) {
         this.pois = pois;
     }
     public List<Pois> getPois() {
         return pois;
     }

    public void setRoads(List<String> roads) {
         this.roads = roads;
     }
     public List<String> getRoads() {
         return roads;
     }

    public void setPoiRegions(List<String> poiRegions) {
         this.poiRegions = poiRegions;
     }
     public List<String> getPoiRegions() {
         return poiRegions;
     }

    public void setSematic_description(String sematic_description) {
         this.sematic_description = sematic_description;
     }
     public String getSematic_description() {
         return sematic_description;
     }

    public void setCityCode(int cityCode) {
         this.cityCode = cityCode;
     }
     public int getCityCode() {
         return cityCode;
     }

}