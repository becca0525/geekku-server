package com.kosta.geekku.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kosta.geekku.config.auth.PrincipalDetails;
import com.kosta.geekku.dto.CompanyDto;
import com.kosta.geekku.entity.Estate;
import com.kosta.geekku.entity.HouseAnswer;
import com.kosta.geekku.entity.OnestopAnswer;
import com.kosta.geekku.entity.Role;
import com.kosta.geekku.service.CompanyService;
import com.kosta.geekku.service.EstateNumberService;

@RestController
public class CompanyController {

	@Autowired
	private CompanyService companyService; 

	@Autowired
	private EstateNumberService estateNumberService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@PostMapping("/joinCompany")
	public ResponseEntity<String> joinCompany(CompanyDto companyDto,
			@RequestParam(name = "file", required = false) MultipartFile file) {
		try {
			String rawPassword = companyDto.getPassword();
			companyDto.setPassword(bCryptPasswordEncoder.encode(rawPassword));

			if ("estate".equals(companyDto.getType()) || "interior".equals(companyDto.getType())) {
				companyDto.setRole(Role.ROLE_COMPANY);
			} else {
				return new ResponseEntity<String>("사업자 타입 오류", HttpStatus.BAD_REQUEST);
			}

			companyService.joinCompany(companyDto, file);
			return new ResponseEntity<String>("기업회원 가입 성공", HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("회원가입 실패", HttpStatus.BAD_REQUEST);
		}
	}


	@GetMapping("/searchEstate")
	public ResponseEntity<String> searchEstate(@RequestParam(required = false) String bsnmCmpnm,
			@RequestParam(required = false) String brkrNm, @RequestParam(required = false) String jurirno) {
		// 브이월드 Settings 출력
		estateNumberService.vworldSettings();
		try {
			String response = estateNumberService.searchEstate(bsnmCmpnm, brkrNm, jurirno);
			return new ResponseEntity<String>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("조회할 수 없습니다", HttpStatus.BAD_REQUEST);
		}
	}
	
	//중개업자 프로필 조회

	@GetMapping("/estateProfile/{companyId}") 
    public ResponseEntity<?> getBrokerProfile(@PathVariable String companyId) {
        try {
            CompanyDto companyProfile = companyService.getCompanyProfile(companyId);
            return ResponseEntity.ok(companyProfile);
        } catch (Exception e) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 정보를 찾을 수 없습니다.");
        }
    }
	
	// 중개업자 쓴 글 보기(수정해야할 수도 있음)
	@GetMapping("/estateCommunities/{companyId}") // 예시: http://localhost:8080/brokerCommunities/7e7506d5-b944-40c8-a269-c3c58d2067bb
	public ResponseEntity<?> getEstateCommunities(@PathVariable String companyId) {
	    try {
	        List<Estate> estate = companyService.getEstateCommunities(companyId);
	        return ResponseEntity.ok(estate);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("중개업자가 작성한 게시글을 찾을 수 없습니다.");
	    }
	}
	
	// 중개업자가 쓴 글 삭제하기 (테스트 해야함)
	@DeleteMapping("/estateCommunityDelete/{estateId}")
	public ResponseEntity<?> deleteEstateCommunity(@PathVariable Integer estateId) {
		try {
			companyService.deleteEstateCommunity(estateId);
			return new ResponseEntity<>("게시글 삭제에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("중개업자가 작성한 게시글을 찾을 수 없습니다.");
		}
	}
	
	// 중개업자 집꾸하기 답변 글 조회 (테스트 해야함) + 페이징 처리
	@GetMapping("/estateAnswered/{companyId}")
	public ResponseEntity<?> getEstateAnswered(@PathVariable UUID companyId, Pageable pageable) {
	    try {
	        Page<HouseAnswer> houseAnswers = companyService.getAnswersByCompanyId(companyId, pageable);
	        return ResponseEntity.ok(houseAnswers);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("중개업자가 작성한 게시글을 찾을 수 없습니다.");
	    }
	}
	
	@GetMapping("/onestopAnswered/{companyId}")
    public ResponseEntity<?> getOnestopAnswered(@PathVariable UUID companyId, Pageable pageable) {
        try {
            // 회사 ID와 페이지 정보를 받아서 조회
            Page<OnestopAnswer> onestopAnswers = companyService.getOnestopAnswersByCompanyId(companyId, pageable);
            return ResponseEntity.ok(onestopAnswers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("중개업자가 작성한 Onestop 답변을 찾을 수 없습니다.");
        }
    }


	@GetMapping("/company/companyInfo")
	public ResponseEntity<CompanyDto> getCompanyInfo(Authentication authentication) {
		String username = ((PrincipalDetails) authentication.getPrincipal()).getCompany().getUsername();
		try {
			CompanyDto companyDto = companyService.getCompany(username);
			return new ResponseEntity<CompanyDto>(companyDto, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<CompanyDto>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping("/company/updateCompanyInfo")
	public ResponseEntity<String> updateCompanyInfo(Authentication authentication, @RequestBody CompanyDto companyDto) {
		try {
			UUID companyId = ((PrincipalDetails) authentication.getPrincipal()).getCompany().getCompanyId();
			companyService.updateCompanyInfo(companyId, companyDto);
			return new ResponseEntity<String>("회원정보 수정 완료", HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("회원정보 수정 실패", HttpStatus.BAD_REQUEST);
		}
		
	}
}
