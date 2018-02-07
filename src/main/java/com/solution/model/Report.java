package com.solution.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Report {
	private int refId;
    private String name;
    private int age;
    
	public int getRefId() {
		return refId;
	}
	public void setRefId(int refId) {
		this.refId = refId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
    
    
}
