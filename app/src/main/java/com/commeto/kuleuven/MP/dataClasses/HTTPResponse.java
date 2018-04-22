package com.commeto.kuleuven.MP.dataClasses;

/**
 * Created by Jonas on 4/03/2018.
 */

public class HTTPResponse{
//==================================================================================================
    //class specs

    private int responseCode;
    private String responseMessge;
    private String responsBody;
    private int id;

    public HTTPResponse(){
        this.id = -1;
        this.responseCode = -2;
        this.responseMessge = "OFFLINE";
        this.responsBody = "no network connection";
    }

    public HTTPResponse(int responseCode, String responseMessge, String responsBody, int id) {
        this.responseCode = responseCode;
        this.responseMessge = responseMessge;
        this.responsBody = responsBody;
        this.id = id;
    }
//==================================================================================================
    //getters

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessge() {
        return responseMessge;
    }

    public String getResponsBody() {
        return responsBody;
    }

    public int getId() {
        return id;
    }
//==================================================================================================
    //setters

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseMessge(String responseMessge) {
        this.responseMessge = responseMessge;
    }

    public void setResponsBody(String responsBody) {
        this.responsBody = responsBody;
    }

    public void setId(int id) {
        this.id = id;
    }
}
