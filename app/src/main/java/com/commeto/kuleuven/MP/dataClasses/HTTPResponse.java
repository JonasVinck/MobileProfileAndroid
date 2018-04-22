package com.commeto.kuleuven.MP.dataClasses;

/**
 * <pre>
 * Created by Jonas on 4/03/2018.
 *
 * Class to bundle the information gotten from an HTTP response message and the id that might have
 * been specified.
 *
 * Attributes:
 *  - responseCode      Response code of the HTTP response.
 *  - responseMessage   Response message of the HTTP response
 *  - responseBody      Body of the HTTP response.
 *  - id                Id of the LocalRoute getting uploaded or updates.
 * </pre>
 */

public class HTTPResponse{
//==================================================================================================
    //class specs

    private int responseCode;
    private String responseMessage;
    private String responseBody;
    private int id;

    public HTTPResponse(){
        this.id = -1;
        this.responseCode = -2;
        this.responseMessage = "OFFLINE";
        this.responseBody = "no network connection";
    }

    public HTTPResponse(int responseCode, String responseMessage, String responseBody, int id) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.responseBody = responseBody;
        this.id = id;
    }
//==================================================================================================
    //getters

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getId() {
        return id;
    }
//==================================================================================================
    //setters

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public void setId(int id) {
        this.id = id;
    }
}
