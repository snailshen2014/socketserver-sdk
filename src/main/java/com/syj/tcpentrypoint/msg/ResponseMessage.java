package com.syj.tcpentrypoint.msg;

/**
 * 
*  @des    :The response message
 * @author:shenyanjun1
 * @date   :2018-12-14 17:05
 */
public class ResponseMessage extends BaseMessage {

	private Object response;
    private Throwable exception; //error when the error has been declare in the interface

    public ResponseMessage(boolean initHeader) {
        super(initHeader);
    }

    public ResponseMessage() {
        super(true);
    }

    public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

	/**
	 * @return the error
	 */
	public boolean isError() {
		return exception != null;
	}

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "header="+ getMsgHeader() +
                "response=" + response +
                ", exception=" + exception +
                '}';
    }
}