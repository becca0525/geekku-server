package com.kosta.geekku.entity;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.apache.tomcat.util.codec.binary.Base64;
import org.hibernate.annotations.CreationTimestamp;

import com.kosta.geekku.dto.HouseAnswerDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class HouseAnswer {
	// 집꾸 답변
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer answerHouseNum;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "houseNum")
	private House house;
	//private Integer houseNum; // join column House -houseNum
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "companyId")
	private Company company;
	//private UUID companyId; // join column Company -companyId
	
	@Column(columnDefinition = "LONGTEXT")
	@Lob
	private String content;
	@CreationTimestamp
	private Timestamp createdAt;
	
	public HouseAnswerDto toDto() {
		HouseAnswerDto houseAnswerDto = HouseAnswerDto.builder()
							.answerHouseNum(answerHouseNum)
							.content(content)
							.createdAt(createdAt)
							.houseNum(house.getHouseNum())
							.companyId(company.getCompanyId())
							.companyName(company.getCompanyName())
							.companyPhone(company.getPhone())
							.build();
		
		if (company.getProfileImage() != null) {
			try {
				houseAnswerDto.setCompanyProfileImage(new String(Base64.encodeBase64(company.getProfileImage()), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return houseAnswerDto;
	}
}
