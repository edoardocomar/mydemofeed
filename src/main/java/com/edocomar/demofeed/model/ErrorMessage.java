package com.edocomar.demofeed.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorMessage {
	private String msg;

    public ErrorMessage() {
		this.msg = "";
	}

    public ErrorMessage(String msg) {
		this.msg = msg;
	}
    
	@JsonProperty
	public String getMsg() {
		return msg;
	}
    @JsonProperty
	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ErrorMessage other = (ErrorMessage) obj;
		if (msg == null) {
			if (other.msg != null)
				return false;
		} else if (!msg.equals(other.msg))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ErrorMessage [msg=" + msg + "]";
	}


}
