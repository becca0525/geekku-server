package com.kosta.geekku.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Auth {
	// 인증관련
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer authNum;
	private String phone;
	private String email;
	private Integer certificationNum;
}
