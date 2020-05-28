package com.redhat.demos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "train")
@XmlAccessorType(XmlAccessType.FIELD)
public class Train {

    int train_id;
    int track_id;
    String station_from;
    String station_to;
    Boolean status = false;


	public int getTrain_id() {
		return train_id;
	}
	public void setTrain_id(int train_id) {
		this.train_id = train_id;
	}
	public int getTrack_id() {
		return track_id;
	}
	public void setTrack_id(int track_id) {
		this.track_id = track_id;
	}
	public String getStation_from() {
		return station_from;
	}
	public void setStation_from(String station_from) {
		this.station_from = station_from;
	}
	public String getStation_to() {
		return station_to;
	}
	public void setStation_to(String station_to) {
		this.station_to = station_to;
	}
	public Boolean getStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
	}
    
}