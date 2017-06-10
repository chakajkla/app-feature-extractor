/**
 * 
 */
package server.objects;

/**
 * @author niessen
 *
 */
public class User
{
    private String deviceId;
    private Integer numberOfApps;
    private String email;
    private Integer appGroup;
    private Boolean secondStage;
    /**
     * @return the deviceId
     */
    public String getDeviceId()
    {
        return deviceId;
    }
    /**
     * @param deviceId the deviceId to set
     */
    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }
    /**
     * @return the numberOfApps
     */
    public Integer getNumberOfApps()
    {
        return numberOfApps;
    }
    /**
     * @param numberOfApps the numberOfApps to set
     */
    public void setNumberOfApps(Integer numberOfApps)
    {
        this.numberOfApps = numberOfApps;
    }
    /**
     * @return the email
     */
    public String getEmail()
    {
        return email;
    }
    /**
     * @param email the email to set
     */
    public void setEmail(String email)
    {
        this.email = email;
    }
    /**
     * @return the app_group
     */
    public Integer getAppGroup()
    {
        return appGroup;
    }
    /**
     * @param appGroup the app_group to set
     */
    public void setAppGroup(Integer appGroup)
    {
        this.appGroup = appGroup;
    }
    /**
     * @return the secondStage
     */
    public Boolean getSecondStage()
    {
        return secondStage;
    }
    /**
     * @param secondStage the secondStage to set
     */
    public void setSecondStage(Boolean secondStage)
    {
        this.secondStage = secondStage;
    }
}
